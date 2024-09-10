package leavegroup

type Message struct {
	GroupID string `json:"groupId" validate:"required"`
	UserID  string `json:"userId"  validate:"required"`
}
