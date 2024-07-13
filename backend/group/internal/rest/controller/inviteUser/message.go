package inviteUser

type Message struct {
	GroupId  string `json:"groupId,omitempty" validate:"required"`
	UserId   string `json:"userId,omitempty" validate:"required"`
	PlayerId string `json:"playerId,omitempty" validate:"required"`
}
