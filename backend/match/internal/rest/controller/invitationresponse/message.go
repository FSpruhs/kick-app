package invitationresponse

type Message struct {
	MatchID            string `json:"match_id" validate:"required"`
	Accept             bool   `json:"accept" validate:"required"`
	RespondingPlayerID string `json:"respondingPlayerId" validate:"required"`
	PlayerID           string `json:"playerId" validate:"required"`
}
