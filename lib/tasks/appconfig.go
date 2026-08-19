package tasks

import (
	"fmt"
	"strings"

	"sweetjuice/plugins/custom"
)

// AppConfig holds runtime configuration parsed from choices.ini.
type AppConfig struct {
	ServerURL string
	HideApp   bool
}

// LoadAppConfig reads choices.ini from the bundled assets and parses key values.
func LoadAppConfig() AppConfig {
	cfg := AppConfig{
		ServerURL: fallbackServerURL,
	}

	raw, err := custom.ReadAsset("choices.ini")
	if err != nil {
		fmt.Println("appconfig: failed to read choices.ini:", err)
		return cfg
	}

	cfg = parseChoicesINI(raw)

	if cfg.ServerURL != "" {
		serverURL = cfg.ServerURL
	}
	if cfg.HideApp {
		hideAppIcon()
	}

	return cfg
}

func parseChoicesINI(raw string) AppConfig {
	cfg := AppConfig{ServerURL: fallbackServerURL}

	section := ""
	lines := strings.Split(raw, "\n")
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" || strings.HasPrefix(line, ";") || strings.HasPrefix(line, "#") {
			continue
		}
		if strings.HasPrefix(line, "[") && strings.HasSuffix(line, "]") {
			section = strings.ToLower(line[1 : len(line)-1])
			continue
		}
		parts := strings.SplitN(line, "=", 2)
		if len(parts) != 2 {
			continue
		}
		key := strings.TrimSpace(strings.ToLower(parts[0]))
		val := strings.TrimSpace(parts[1])

		switch section {
		case "behavior":
			switch key {
			case "remote_url":
				cfg.ServerURL = val
			case "hideapp":
				cfg.HideApp = strings.EqualFold(val, "true") || val == "1"
			}
		}
	}

	return cfg
}

func hideAppIcon() {
	fmt.Println("appconfig: hideapp=true requested; launcher visibility should be toggled via native code")
}
