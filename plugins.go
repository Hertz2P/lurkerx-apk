package juiceapp

import (
	"fmt"

	"sweetjuice/plugins/custom"

	"github.com/sweet-juice/sweetjuice/app"
	apppkg "github.com/sweet-juice/sweetjuice/plugins/app"
	"github.com/sweet-juice/sweetjuice/plugins/biometric"
	"github.com/sweet-juice/sweetjuice/plugins/broadcast"
	"github.com/sweet-juice/sweetjuice/plugins/calls"
	"github.com/sweet-juice/sweetjuice/plugins/daemon"
	"github.com/sweet-juice/sweetjuice/plugins/datadir"
	"github.com/sweet-juice/sweetjuice/plugins/datastore"
	"github.com/sweet-juice/sweetjuice/plugins/deeplinking"
	"github.com/sweet-juice/sweetjuice/plugins/devicestate"
	"github.com/sweet-juice/sweetjuice/plugins/filepicker"
	"github.com/sweet-juice/sweetjuice/plugins/gps"
	"github.com/sweet-juice/sweetjuice/plugins/logger"
	"github.com/sweet-juice/sweetjuice/plugins/mu3"
	"github.com/sweet-juice/sweetjuice/plugins/notification"
	"github.com/sweet-juice/sweetjuice/plugins/notification_listener"
	"github.com/sweet-juice/sweetjuice/plugins/permission"
	"github.com/sweet-juice/sweetjuice/plugins/sms"
	"github.com/sweet-juice/sweetjuice/plugins/special"
	"github.com/sweet-juice/sweetjuice/plugins/system"
	"github.com/sweet-juice/sweetjuice/plugins/workmanager"
)

// registerPluginDefinitions registers plugin metadata with Java without needing the app instance.
// Call this BEFORE app.Run() so GetRegisteredPlugins() returns the full list for the first render.
func registerPluginDefinitions() {
	RegisterPlugin("app", "com.sweetjuice.pkg.app", "AppPlugin")
	RegisterPlugin("mu3", "com.sweetjuice.pkg.mu3", "Mu3Plugin")
	RegisterPlugin("permissions", "com.sweetjuice.pkg.permissions", "PermissionsPlugin")
	RegisterPlugin("special", "com.sweetjuice.pkg.special", "SpecialPermissionsPlugin")
	RegisterPlugin("gps", "com.sweetjuice.pkg.gps", "GpsPlugin")
	RegisterPlugin("sms", "com.sweetjuice.pkg.sms", "SmsPlugin")
	RegisterPlugin("calls", "com.sweetjuice.pkg.calls", "CallsPlugin")
	RegisterPlugin("biometric", "com.sweetjuice.pkg.biometric", "BiometricPlugin")
	RegisterPlugin("broadcast", "com.sweetjuice.pkg.broadcast", "BroadcastPlugin")
	RegisterPlugin("daemon", "com.sweetjuice.pkg.daemon", "DaemonPlugin")
	RegisterPlugin("datadir", "com.sweetjuice.pkg.datadir", "DataDirPlugin")
	RegisterPlugin("datastore", "com.sweetjuice.pkg.datastore", "DataStorePlugin")
	RegisterPlugin("deeplinking", "com.sweetjuice.pkg.deeplinking", "DeepLinkingPlugin")
	RegisterPlugin("devicestate", "com.sweetjuice.pkg.devicestate", "DeviceStatePlugin")
	RegisterPlugin("filepicker", "com.sweetjuice.pkg.filepicker", "FilePickerPlugin")
	RegisterPlugin("logger", "com.sweetjuice.pkg.logger", "LoggerPlugin")
	RegisterPlugin("notification", "com.sweetjuice.pkg.notifications", "NotificationPlugin")
	RegisterPlugin("system", "com.sweetjuice.pkg.system", "SystemPlugin")
	RegisterPlugin("custom", "com.custompackage", "CustomPlugin")
	RegisterPlugin("workmanager", "com.sweetjuice.pkg.workmanager", "WorkManagerPlugin")
}

// initPlugins initializes all plugins with the app instance.
// Call this AFTER app.Run() so plugins can initialize native methods and event handlers.
func initPlugins() {
	a := app.GetGlobalApp()
	if a == nil {
		fmt.Println("initPlugins: global app not ready")
		return
	}

	apppkg.NewAppPlugin().Init(a)
	mu3.New().Init(a)
	permission.NewPlugin().Init(a)
	special.NewPlugin().Init(a)
	gps.NewPlugin().Init(a)
	sms.NewPlugin().Init(a)
	calls.NewPlugin().Init(a)
	biometric.NewPlugin().Init(a)
	broadcast.NewPlugin().Init(a)
	daemon.NewPlugin().Init(a)
	datadir.NewPlugin().Init(a)
	datastore.NewPlugin().Init(a)
	deeplinking.NewPlugin().Init(a)
	devicestate.NewPlugin().Init(a)
	filepicker.NewPlugin().Init(a)
	logger.NewPlugin("sweetjuice").Init(a)
	notification.NewPlugin().Init(a)
	notification_listener.NewPlugin().Init(a)
	custom.NewPlugin().Init(a)
	system.NewPlugin().Init(a)
	workmanager.NewPlugin().Init(a)
}
