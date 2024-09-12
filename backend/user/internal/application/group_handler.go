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
	users    domain.UserRepository
}

func NewGroupHandler(messages domain.MessageRepository, users domain.UserRepository) *GroupHandler[ddd.AggregateEvent] {
	return &GroupHandler[ddd.AggregateEvent]{messages: messages, users: users}
}

func (h GroupHandler[T]) HandleEvent(event ddd.AggregateEvent) error {
	switch event.EventName() {
	case grouppb.UserInvitedEvent:
		return h.onUserInvitedEvent(event)
	case grouppb.UserAcceptedInvitationEvent:
		return h.onUserAcceptedInvitationEvent(event)
	case grouppb.GroupCreatedEvent:
		return h.onGroupCreatedEvent(event)
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
		return fmt.Errorf("creating user invited message: %w", err)
	}

	return nil
}

func (h GroupHandler[T]) onUserAcceptedInvitationEvent(event ddd.Event) error {
	userAccepted, ok := event.Payload().(grouppb.UserAcceptedInvitation)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	if err := h.addGroupToUser(userAccepted.UserID, userAccepted.GroupID); err != nil {
		return fmt.Errorf("adding group to user: %w", err)
	}

	return nil
}

func (h GroupHandler[T]) onGroupCreatedEvent(event ddd.Event) error {
	groupCreated, ok := event.Payload().(grouppb.GroupCreated)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	if err := h.addGroupToUser(groupCreated.UserIDs[0], groupCreated.GroupID); err != nil {
		return fmt.Errorf("adding group to user: %w", err)
	}

	return nil
}

func (h GroupHandler[T]) addGroupToUser(userID, groupID string) error {
	user, err := h.users.FindByID(userID)
	if err != nil {
		return fmt.Errorf("finding user by id: %w", err)
	}

	user.JoinGroup(groupID)

	if err := h.users.Save(user); err != nil {
		return fmt.Errorf("saving user: %w", err)
	}

	return nil
}
