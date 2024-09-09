package leavegroup

type Message struct {
	GroupID string `json:"groupId" binding:"required"`
	UserID  string `json:"userId" binding:"required"`
}
