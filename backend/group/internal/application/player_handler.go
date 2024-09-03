package application

import (
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
)

type PlayerHandler[T ddd.AggregateEvent] struct{}

func NewPlayerHandler() *PlayerHandler[ddd.AggregateEvent] {
	return &PlayerHandler[ddd.AggregateEvent]{}
}

func (h PlayerHandler[T]) HandleEvent(event ddd.AggregateEvent) error {
	switch event.EventName() {
	case playerspb.NewMasterAppointedEvent:
		return h.onNewMasterAppointedEvent(event)
	}
	return nil
}

func (h PlayerHandler[T]) onNewMasterAppointedEvent(event ddd.Event) error {
	newMasterAppointed, ok := event.Payload().(playerspb.NewMasterAppointed)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	// TODO implement the logic to handle the event
	print(newMasterAppointed)

	return nil
}
