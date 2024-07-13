package domain

import "time"

type Message struct {
	ID         string
	UserId     string
	Content    string
	Type       MessageType
	OccurredAt time.Time
	Read       bool
}
