package addregistration

type Message struct {
	UserID       string `json:"userId"     validate:"required"`
	MatchID      string `json:"matchId"    validate:"required"`
	AddingUserID string `json:"addingUserId" validate:"required"`
}
