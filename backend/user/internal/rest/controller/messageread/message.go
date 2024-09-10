package messageread

type Message struct {
	UserID    string `json:"user_id"    validate:"required"`
	MessageID string `json:"message_id" validate:"required"`
	Read      bool   `json:"read"       validate:"required"`
}
