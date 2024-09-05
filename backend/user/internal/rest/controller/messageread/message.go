package messageread

type Message struct {
	UserID    string `json:"user_id" binding:"required"`
	MessageID string `json:"message_id" binding:"required"`
	Read      bool   `json:"read" binding:"required"`
}
