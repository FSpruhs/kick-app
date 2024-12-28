package application

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/matchpb"
)

type MatchHandler[T ddd.AggregateEvent] struct {
	groups         domain.GroupRepository
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent]
}

func NewMatchHandler(groups domain.GroupRepository, eventPublisher ddd.EventPublisher[ddd.AggregateEvent]) *MatchHandler[ddd.AggregateEvent] {
	return &MatchHandler[ddd.AggregateEvent]{groups: groups, eventPublisher: eventPublisher}
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

	group, err := h.groups.FindByID(matchCreated.GroupID)
	if err != nil {
		return fmt.Errorf("finding group by id: %w", err)
	}

	group.MatchCreatedForGroup(matchCreated.MatchID)

	if err := h.eventPublisher.Publish(group.Events()...); err != nil {
		return fmt.Errorf("publishing match created for group event: %w", err)
	}

	return nil
}
