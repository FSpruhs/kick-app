package removeuser

type Message struct {
	GroupID        string `json:"groupId"        validate:"required"`
	RemoveUserID   string `json:"removeUserId"   validate:"required"`
	RemovingUserID string `json:"removingUserId" validate:"required"`
}
