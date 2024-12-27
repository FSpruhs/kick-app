package creatematch

type Message struct {
	UserID     string `json:"userId"     validate:"required"`
	GroupID    string `json:"groupId"    validate:"required"`
	Begin      string `json:"begin"      validate:"required"`
	Location   string `json:"location"   validate:"required"`
	MaxPlayers int    `json:"maxPlayers" validate:"required"`
	MinPlayers int    `json:"minPlayers" validate:"required"`
}
