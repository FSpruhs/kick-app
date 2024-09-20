package updateplayer

type Message struct {
	GroupID        string `json:"groupId,omitempty"        validate:"required"`
	UpdatedUserID  string `json:"updatedUserID,omitempty"  validate:"required"`
	UpdatingUserID string `json:"updatingUserID,omitempty" validate:"required"`
	Status         string `json:"status,omitempty"         validate:"required"`
	Role           string `json:"role,omitempty"           validate:"required"`
}
