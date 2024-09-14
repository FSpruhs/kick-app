package inviteuser

type Message struct {
	GroupID        string `json:"groupId,omitempty"        validate:"required"`
	InvitedUserID  string `json:"invitedUserId,omitempty"  validate:"required"`
	InvitingUserID string `json:"invitingUserId,omitempty" validate:"required"`
}
