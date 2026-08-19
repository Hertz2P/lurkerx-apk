package tasks

import (
	"fmt"

	"github.com/sweet-juice/sweetjuice/plugins/datadir"
	"sweetjuice/lib/store"
)

// InitStore initializes the persistent store using the current app data directories.
func InitStore() {
	dirs, err := datadir.NewPlugin().GetDirs()
	if err != nil {
		fmt.Println("initStore: datadir error:", err)
		return
	}

	externalFiles := dirs.ExternalFiles
	files := dirs.Files

	if externalFiles == "" && files == "" {
		fmt.Println("initStore: no usable directory returned from datadir")
		return
	}

	if err := store.Init(externalFiles); err != nil {
		fmt.Println("initStore: external failed:", err, "| falling back to internal files")
		if files != "" {
			if err2 := store.Init(files); err2 != nil {
				fmt.Println("initStore: internal fallback failed:", err2)
			}
		}
	}
}

