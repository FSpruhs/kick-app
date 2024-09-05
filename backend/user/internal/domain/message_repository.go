package domain

type MessageRepository interface {
	Create(message Message) error
	FindById(id string) (Message, error)
}
