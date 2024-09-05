package inviteduserresponse

type Message struct {
	GroupID string `json:"group_id"`
	UserID  string `json:"user_id"`
	Accept  bool   `json:"accept"`
}
