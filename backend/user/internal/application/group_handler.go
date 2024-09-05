package application

import (
	"fmt"
	"time"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GroupHandler[T ddd.AggregateEvent] struct {
	messages domain.MessageRepository
}

func NewGroupHandler(messages domain.MessageRepository) *GroupHandler[ddd.AggregateEvent] {
	return &GroupHandler[ddd.AggregateEvent]{messages: messages}
}

func (h GroupHandler[T]) HandleEvent(event ddd.AggregateEvent) error {
	switch event.EventName() {
	case grouppb.UserInvitedEvent:
		return h.onUserInvitedEvent(event)
	}

	return nil
}

func (h GroupHandler[T]) onUserInvitedEvent(event ddd.Event) error {
	userInvited, ok := event.Payload().(grouppb.UserInvited)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	message := domain.Message{
		ID:         uuid.New().String(),
		UserID:     userInvited.UserID,
		Content:    fmt.Sprintf("You have been invited to %s!", userInvited.GroupName),
		Type:       domain.GroupInvitation,
		OccurredAt: time.Now(),
		Read:       false,
	}

	if err := h.messages.Create(&message); err != nil {
		return fmt.Errorf("while creating db err: %w", err)
	}

	return nil
}
