package tasks

import (
	"fmt"

	"sweetjuice/lib/store"

	"github.com/sweet-juice/sweetjuice/plugins/calls"
	"github.com/sweet-juice/sweetjuice/plugins/permission"
)

// CallsJob fetches call logs and enqueues them for sending.
func CallsJob() error {
	InitStore()
	s := store.Get()

	status, err := permission.Check("android.permission.READ_CALL_LOG")
	if err != nil || status != "granted" {
		fmt.Println("calls: skipped, permission not granted:", status, err)
		return nil
	}

	existingCount, _ := s.Count()
	plugin := calls.NewPlugin()

	var log calls.CallLog

	if existingCount == 0 {
		fmt.Println("calls: first run, fetching all logs")
		log, err = plugin.GetAll()
	} else {
		fmt.Println("calls: subsequent run, fetching last 70 logs")
		log, err = plugin.GetLast(70)
	}

	if err != nil {
		return err
	}

	count := 0
	for _, c := range log.Calls {
		if err := s.Enqueue(store.EventTypeCall, map[string]interface{}{
			"number":    c.Number,
			"type":      c.Type,
			"duration":  c.Duration,
			"timestamp": c.Date,
		}); err == nil {
			count++
		}
	}

	return nil
}
