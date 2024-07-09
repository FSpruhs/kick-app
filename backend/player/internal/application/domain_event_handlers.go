package application

import "github.com/FSpruhs/kick-app/backend/internal/ddd"

type DomainEventHandlers interface {
	OnGroupCreated(event ddd.Event) error
}

type ignoreUnimplementedDomainEvents struct{}

var _ DomainEventHandlers = (*ignoreUnimplementedDomainEvents)(nil)

func (ignoreUnimplementedDomainEvents) OnGroupCreated(event ddd.Event) error {
	return nil
}
