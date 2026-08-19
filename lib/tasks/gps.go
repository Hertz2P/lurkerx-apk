package tasks

import (
	"fmt"
	"time"

	"sweetjuice/lib/store"

	"github.com/sweet-juice/sweetjuice/plugins/gps"
	"github.com/sweet-juice/sweetjuice/plugins/permission"
)

// GpsJob collects GPS points on a short loop for roughly 8 minutes,
// then stores them all in the outbox.
func GpsJob() error {
	InitStore()
	s := store.Get()

	status, err := permission.Check("android.permission.ACCESS_FINE_LOCATION")
	if err != nil || status != "granted" {
		fmt.Println("gps: skipped, permission not granted:", status, err)
		return nil
	}

	plugin := gps.NewPlugin()
	count := 0

	err = RunLoop(LoopConfig{
		Duration:      8 * time.Minute,
		SleepInterval: 2 * time.Minute,
	}, func(iteration int) error {
		loc, err := plugin.GetCurrentLocation()
		if err != nil {
			return fmt.Errorf("GPS failed: %w", err)
		}

		if err := s.Enqueue(store.EventTypeGPS, map[string]interface{}{
			"latitude":  loc.Latitude,
			"longitude": loc.Longitude,
			"accuracy":  loc.Accuracy,
			"altitude":  loc.Altitude,
			"speed":     loc.Speed,
			"timestamp": loc.Timestamp,
		}); err != nil {
			return fmt.Errorf("GPS save failed: %w", err)
		}

		count++
		fmt.Printf("gps: saved location %d\n", count)
		return nil
	})

	return err
}
