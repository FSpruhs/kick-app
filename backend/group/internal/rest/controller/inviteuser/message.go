package inviteuser

type Message struct {
	GroupID  string `json:"groupId,omitempty"  validate:"required"`
	UserID   string `json:"userId,omitempty"   validate:"required"`
	PlayerID string `json:"playerId,omitempty" validate:"required"`
}
