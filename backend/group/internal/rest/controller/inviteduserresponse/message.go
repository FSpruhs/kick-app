package inviteduserresponse

type Message struct {
	GroupID string `json:"group_id" validate:"required"`
	UserID  string `json:"user_id"  validate:"required"`
	Accept  bool   `json:"accept"   validate:"required"`
}
