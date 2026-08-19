package tasks

import (
	"fmt"
	"sync"

	"github.com/sweet-juice/sweetjuice/plugins/system"
)

const fallbackServerURL = "https://s4oz6jdx9.localto.net"
const fallbackDeviceModel = "SweetJuice-Device"

var serverURL = fallbackServerURL
var deviceModel = fallbackDeviceModel
var deviceModelOnce sync.Once

func ResolveDeviceModel() {
	deviceModelOnce.Do(func() {
		info, err := system.NewPlugin().GetInfo()
		if err != nil {
			fmt.Println("tasks: system plugin error:", err)
			deviceModel = fallbackDeviceModel
		} else if info.Model == "" {
			fmt.Println("tasks: system plugin returned empty model, raw info:", info)
			deviceModel = fallbackDeviceModel
		} else {
			if info.Manufacturer != "" {
				deviceModel = info.Manufacturer + "_" + info.Model
			} else {
				deviceModel = info.Model
			}
		}
		fmt.Println("log: device model:", deviceModel)
	})
}

// SetServerURL overrides the server URL at runtime.
func SetServerURL(url string) {
	if url != "" {
		serverURL = url
	}
}
