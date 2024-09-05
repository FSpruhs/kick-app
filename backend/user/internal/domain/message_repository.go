package domain

type MessageRepository interface {
	Create(message *Message) error
	FindByID(id string) (*Message, error)
	Save(message *Message) error
}
