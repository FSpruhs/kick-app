package getusermessages

import "time"

type Response struct {
	ID         string    `json:"id"`
	UserID     string    `json:"userId"`
	Content    string    `json:"content"`
	Type       string    `json:"type"`
	OccurredAt time.Time `json:"occurredAt"`
	Read       bool      `json:"read"`
}
