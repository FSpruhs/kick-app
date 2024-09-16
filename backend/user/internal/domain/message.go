package domain

import "time"

type Message struct {
	ID         string
	UserID     string
	GroupID    string
	Content    string
	Type       MessageType
	OccurredAt time.Time
	Read       bool
}
