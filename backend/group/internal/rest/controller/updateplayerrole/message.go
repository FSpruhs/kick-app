package updateplayerrole

type Message struct {
	GroupID        string `json:"groupId"        validate:"required"`
	UpdatingUserID string `json:"updatingUserId" validate:"required"`
	UpdatedUserID  string `json:"updatedUserId"  validate:"required"`
	NewRole        string `json:"newRole"        validate:"required"`
}
