package application

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"github.com/google/uuid"
)

type GroupHandler[T ddd.AggregateEvent] struct {
	players domain.PlayerRepository
	ignoreUnimplementedDomainEvents
}

func NewGroupHandler(players domain.PlayerRepository) *GroupHandler[ddd.AggregateEvent] {
	return &GroupHandler[ddd.AggregateEvent]{players: players}
}

func (h GroupHandler[T]) HandleEvent(event ddd.AggregateEvent) error {
	switch event.EventName() {
	case grouppb.GroupCreatedEvent:
		return h.onGroupCreatedEvent(event)
	}
	return nil
}

func (h GroupHandler[T]) onGroupCreatedEvent(event ddd.Event) error {
	orderCreated := event.Payload().(grouppb.GroupCreated)
	newPlayer := domain.Player{
		Id:      uuid.New().String(),
		GroupId: orderCreated.GroupID,
		UserId:  orderCreated.UserIds[0],
	}

	_, err := h.players.Create(&newPlayer)
	if err != nil {
		return err
	}
	return nil
}
