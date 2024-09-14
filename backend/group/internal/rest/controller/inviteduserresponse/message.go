package inviteduserresponse

type Message struct {
	GroupID  string `json:"groupId"  validate:"required"`
	UserID   string `json:"userId"   validate:"required"`
	Accepted bool   `json:"accepted" validate:"required"`
}
