package custom

import (
	"encoding/json"

	"github.com/sweet-juice/sweetjuice/core"
)

// CustomPlugin provides app-level actions such as switching to LaunchActivity.
type CustomPlugin struct {
	app *core.Application
}

// NewPlugin creates a new instance of CustomPlugin.
func NewPlugin() *CustomPlugin {
	return &CustomPlugin{}
}

// Init registers the native callback for custom actions.
func (p *CustomPlugin) Init(app *core.Application) error {
	p.app = app
	return nil
}

// ShowLaunch switches from the main SweetJuiceActivity to com.custompackage.LaunchActivity.
func ShowLaunch() (string, error) {
	result := core.CallNativePlatform("custom:show_launch", "[]")
	return result, nil
}

// ReadAsset reads a file from the app's bundled assets by filename.
func ReadAsset(filename string) (string, error) {
	payload, _ := json.Marshal(map[string]string{"filename": filename})
	result := core.CallNativePlatform("custom:read_asset", string(payload))
	return result, nil
}
