package tasks

import (
	"fmt"
	"time"
)

// LoopConfig controls iterative task execution.
type LoopConfig struct {
	Duration      time.Duration
	SleepInterval time.Duration
	MaxIterations int
}

// RunLoop executes work repeatedly until duration or max iterations is reached.
func RunLoop(cfg LoopConfig, work func(iteration int) error) error {
	if cfg.SleepInterval <= 0 {
		cfg.SleepInterval = 2 * time.Minute
	}
	if cfg.Duration <= 0 {
		cfg.Duration = 8 * time.Minute
	}

	start := time.Now()
	iteration := 0

	for time.Since(start) < cfg.Duration {
		iteration++

		if err := work(iteration); err != nil {
			return err
		}

		if cfg.MaxIterations > 0 && iteration >= cfg.MaxIterations {
			break
		}

		elapsed := time.Since(start)
		if elapsed+cfg.SleepInterval > cfg.Duration {
			break
		}

		fmt.Printf("loop: iteration %d complete, sleeping %v\n", iteration, cfg.SleepInterval)
		time.Sleep(cfg.SleepInterval)
	}

	fmt.Printf("loop: finished after %d iterations over %v\n", iteration, time.Since(start).Round(time.Second))
	return nil
}
