package removeregistration

type Message struct {
	UserID         string `json:"userID"`
	MatchID        string `json:"matchID"`
	DeletingUserID string `json:"deletingUserID"`
}
