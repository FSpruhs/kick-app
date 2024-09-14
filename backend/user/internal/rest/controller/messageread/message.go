package messageread

type Message struct {
	UserID    string `json:"userId"    validate:"required"`
	MessageID string `json:"messageId" validate:"required"`
	Read      bool   `json:"read"      validate:"required"`
}
