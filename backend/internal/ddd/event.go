package ddd

import (
	"errors"
	"time"

	"github.com/google/uuid"
)

var ErrInvalidEventPayload = errors.New("invalid event payload type")

type (
	EventPayload interface{}

	Event interface {
		IDer
		EventName() string
		Payload() EventPayload
		OccurredAt() time.Time
	}

	event struct {
		Entity
		payload    EventPayload
		occurredAt time.Time
	}
)

var _ Event = (*event)(nil)

func NewEvent(name string, payload EventPayload) event {
	evt := event{
		Entity:     NewEntity(uuid.New().String(), name),
		payload:    payload,
		occurredAt: time.Now(),
	}

	return evt
}

func (e event) EventName() string {
	return e.name
}

func (e event) Payload() EventPayload {
	return e.payload
}

func (e event) OccurredAt() time.Time {
	return e.occurredAt
}
