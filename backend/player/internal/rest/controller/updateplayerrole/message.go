package updateplayerrole

type Message struct {
	UpdatingPlayerID string `json:"updatingPlayerId,omitempty" validate:"required"`
	PlayerToUpdateID string `json:"playerToUpdateId,omitempty" validate:"required"`
	NewRole          string `json:"newRole,omitempty" validate:"required"`
}
