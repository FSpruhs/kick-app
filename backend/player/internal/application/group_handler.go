package application

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"github.com/google/uuid"
)

type GroupHandler[T ddd.AggregateEvent] struct {
	players domain.PlayerRepository
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
	orderCreated, ok := event.Payload().(grouppb.GroupCreated)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	newPlayer := domain.Player{
		Aggregate: ddd.NewAggregate(uuid.New().String(), domain.PlayerAggregate),
		GroupID:   orderCreated.GroupID,
		UserID:    orderCreated.UserIDs[0],
		Role:      domain.Master,
	}

	_, err := h.players.Create(&newPlayer)
	if err != nil {
		return fmt.Errorf("while creating db err: %w", err)
	}

	return nil
}
