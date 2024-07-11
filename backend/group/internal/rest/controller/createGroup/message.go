package createGroup

type Message struct {
	Name   string `json:"name,omitempty" validate:"required"`
	UserId string `json:"userId,omitempty" validate:"required"`
}
