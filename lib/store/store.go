package store

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"os"
	"sync"
	"time"

	_ "modernc.org/sqlite"
)

const dbName = "outbox.db"

type EventType string

const (
	EventTypeCall  EventType = "call"
	EventTypeSMS   EventType = "sms"
	EventTypeGPS   EventType = "gps"
	EventTypeNotif EventType = "notification"
)

type Item struct {
	ID        string
	Type      EventType
	Payload   map[string]interface{}
	Sent      bool
	Timestamp int64
	CreatedAt int64
}

type Store struct {
	mu    sync.Mutex
	db    *sql.DB
	ready bool
}

var global *Store

func Init(dir string) error {
	s := Get()
	s.mu.Lock()
	defer s.mu.Unlock()

	path := dbName
	if dir != "" {
		path = dir + "/" + dbName
		if err := os.MkdirAll(dir, 0755); err != nil {
			return fmt.Errorf("store: mkdir: %w", err)
		}
	}

	fmt.Println("store: opening database at:", path)
	db, err := sql.Open("sqlite", path)
	if err != nil {
		return fmt.Errorf("store: open: %w", err)
	}

	_, err = db.Exec(`CREATE TABLE IF NOT EXISTS outbox (
		id TEXT PRIMARY KEY,
		type TEXT,
		payload TEXT,
		sent INTEGER DEFAULT 0,
		created_at INTEGER,
		timestamp INTEGER
	)`)
	if err != nil {
		return fmt.Errorf("store: table: %w", err)
	}

	s.db = db
	s.ready = true
	fmt.Println("store: initialized successfully")
	return nil
}

func Get() *Store {
	if global == nil {
		global = &Store{}
	}
	return global
}

func (s *Store) Enqueue(t EventType, payload map[string]interface{}) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.db == nil {
		return fmt.Errorf("store: not initialized")
	}

	id := fmt.Sprintf("%d-%d", time.Now().UnixNano(), time.Now().UnixMilli())
	ts := time.Now().UnixMilli()
	data, _ := json.Marshal(payload)

	_, err := s.db.Exec(
		"INSERT OR REPLACE INTO outbox (id, type, payload, sent, created_at, timestamp) VALUES (?, ?, ?, 0, ?, ?)",
		id, string(t), string(data), ts, ts,
	)
	return err
}

func (s *Store) Unsent() ([]Item, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.db == nil {
		return nil, nil
	}

	rows, err := s.db.Query("SELECT id, type, payload, sent, created_at, timestamp FROM outbox WHERE sent = 0 ORDER BY created_at ASC")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []Item
	for rows.Next() {
		var it Item
		var payloadStr string
		var sentInt int
		if err := rows.Scan(&it.ID, &it.Type, &payloadStr, &sentInt, &it.CreatedAt, &it.Timestamp); err != nil {
			return nil, err
		}
		it.Sent = sentInt != 0
		json.Unmarshal([]byte(payloadStr), &it.Payload)
		items = append(items, it)
	}
	return items, nil
}

func (s *Store) MarkSent(ids ...string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.db == nil {
		return
	}
	for _, id := range ids {
		_, _ = s.db.Exec("UPDATE outbox SET sent = 1 WHERE id = ?", id)
	}
}

func (s *Store) Count() (int, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.db == nil {
		return 0, fmt.Errorf("store: not initialized")
	}
	var count int
	err := s.db.QueryRow("SELECT COUNT(*) FROM outbox").Scan(&count)
	return count, err
}
