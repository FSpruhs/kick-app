package getusermessages

import "time"

type Response struct {
	ID         string
	UserID     string
	Content    string
	Type       string
	OccurredAt time.Time
	Read       bool
}
