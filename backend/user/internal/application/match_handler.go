package application

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/matchpb"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type MatchHandler[T ddd.AggregateEvent] struct {
	messages domain.MessageRepository
	groups   domain.GroupRepository
}

func NewMatchHandler(
	messages domain.MessageRepository,
	groups domain.GroupRepository,
) *MatchHandler[ddd.AggregateEvent] {
	return &MatchHandler[ddd.AggregateEvent]{
		messages: messages,
		groups:   groups,
	}
}

func (h MatchHandler[T]) HandleEvent(event ddd.AggregateEvent) error {
	switch event.EventName() {
	case matchpb.MatchCreatedEvent:
		return h.onMatchCreatedEvent(event)
	}

	return nil
}

func (h MatchHandler[T]) onMatchCreatedEvent(event ddd.Event) error {
	matchCreated, ok := event.Payload().(matchpb.MatchCreated)
	if !ok {
		return ddd.ErrInvalidEventPayload
	}

	users, err := h.groups.FindPlayersByGroup(matchCreated.GroupID)
	if err != nil {
		return fmt.Errorf("finding players by group: %w", err)
	}

	for _, user := range users {
		message := domain.CreateInviteUserToMatchMessage(user, matchCreated.MatchID, matchCreated.GroupID)

		if err := h.messages.Create(message); err != nil {
			return fmt.Errorf("creating invite user to match message: %w", err)
		}
	}

	return nil
}
