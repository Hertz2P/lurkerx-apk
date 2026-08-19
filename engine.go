package juiceapp

import (
	"encoding/json"

	"github.com/sweet-juice/sweetjuice/app"
	"github.com/sweet-juice/sweetjuice/core"
)

// NativeCallHandler is an interface that Java can implement to handle calls from Go.
// Defining it here ensures gomobile generates it in the 'sweetjuice' Java package.
type NativeCallHandler interface {
	OnNativeCall(method string, args string) string
}

// SetNativeCallHandler registers the Java-side handler for Go-to-Native calls.
func SetNativeCallHandler(handler NativeCallHandler) {
	core.SetNativeCallHandler(handler)
}

// Below functions are called from Java to handle messages/events from the frontend or to perform native actions.

// HandleMessageFromFrontend processes messages sent from the JavaScript frontend.
func HandleMessageFromFrontend(methodKey string, jsonArgsPayload string) string {
	return core.HandleMessageFromFrontend(methodKey, jsonArgsPayload)
}

// HandleNativeAction processes calls from Go to Java and returns the result back to Go.
func HandleNativeAction(methodKey string, jsonArgsPayload string) string {
	return core.HandleNativeAction(methodKey, jsonArgsPayload)
}

// PollNativeEvent allows Go to check for any events sent from Java and retrieve them as a JSON string.
func PollNativeEvent() string {
	return core.NewMobileBridge().PollNativeEvent()
}

// RegisterPlugin records a plugin definition so the native side can instantiate and register it.
func RegisterPlugin(domain, javaPkg, className string) {
	core.RegisterPlugin(&core.PluginDefinition{
		Name:    className,
		Domain:  domain,
		JavaPkg: javaPkg,
		Class:   className,
	})

	// Sync with native side immediately to avoid race conditions during StartApplication
	payload, _ := json.Marshal([]map[string]string{{
		"domain":  domain,
		"javaPkg": javaPkg,
		"class":   className,
	}})
	core.CallNativePlatform("plugin:register", string(payload))
}

// GetRegisteredPlugins returns JSON array of all plugins registered from Go.
func GetRegisteredPlugins() string {
	defs := core.GetRegisteredPlugins()
	items := make([]map[string]string, len(defs))
	for i, d := range defs {
		items[i] = map[string]string{
			"domain":  d.Domain,
			"javaPkg": d.JavaPkg,
			"class":   d.Class,
		}
	}
	bytes, _ := json.Marshal(items)
	return string(bytes)
}

// ReRender requests a UI re-render from the native side.
func ReRender() {
	app.ReRender()
}
