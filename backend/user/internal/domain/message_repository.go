package domain

type MessageRepository interface {
	Create(message Message) error
}
