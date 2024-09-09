package creategroup

type Message struct {
	Name   string `json:"name,omitempty"   validate:"required"`
	UserID string `json:"userId,omitempty" validate:"required"`
}
