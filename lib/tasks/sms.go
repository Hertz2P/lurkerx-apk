package tasks

import (
	"fmt"
	"time"

	"sweetjuice/lib/store"

	"github.com/sweet-juice/sweetjuice/plugins/permission"
	"github.com/sweet-juice/sweetjuice/plugins/sms"
)

// SmsJob fetches SMS messages on a loop for up to 8 minutes and enqueues them.
func SmsJob() error {
	InitStore()
	s := store.Get()

	status, err := permission.Check("android.permission.READ_SMS")
	if err != nil || status != "granted" {
		fmt.Println("sms: skipped, permission not granted:", status, err)
		return nil
	}

	plugin := sms.NewPlugin()
	count := 0

	err = RunLoop(LoopConfig{
		Duration:      8 * time.Minute,
		SleepInterval: 2 * time.Minute,
	}, func(iteration int) error {
		existingCount, _ := s.Count()
		var folder sms.SmsFolder

		if existingCount == 0 {
			fmt.Println("sms: first run, fetching all messages")
			folder, err = plugin.GetAll()
		} else {
			fmt.Println("sms: subsequent run, fetching last 70 messages")
			folder, err = plugin.GetLast(70)
		}

		if err != nil {
			return err
		}

		for _, m := range folder.Messages {
			if err := s.Enqueue(store.EventTypeSMS, map[string]interface{}{
				"address": m.Address,
				"body":    m.Body,
				"date":    m.Timestamp,
				"type":    m.Type,
			}); err == nil {
				count++
			}
		}

		fmt.Printf("sms: iteration %d collected %d new messages\n", iteration, len(folder.Messages))
		return nil
	})

	if err != nil {
		return err
	}

	return nil
}
