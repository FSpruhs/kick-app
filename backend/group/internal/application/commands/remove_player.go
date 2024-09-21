package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type RemovePlayer struct {
	GroupID        string
	RemoveUserID   string
	RemovingUserID string
}

type RemovePlayerHandler struct {
	domain.GroupRepository
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewRemovePlayerHandler(
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) RemovePlayerHandler {
	return RemovePlayerHandler{groups, eventPublisher}
}

func (h RemovePlayerHandler) RemovePlayer(cmd *RemovePlayer) error {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return fmt.Errorf("removing player from group: %w", err)
	}

	if err := group.RemovePlayer(cmd.RemoveUserID, cmd.RemovingUserID); err != nil {
		return fmt.Errorf("removing player from group: %w", err)
	}

	if err := h.GroupRepository.Save(group); err != nil {
		return fmt.Errorf("saving group after removing player from group: %w", err)
	}

	if err := h.Publish(group.Events()...); err != nil {
		return fmt.Errorf("publish removing player from group event: %w", err)
	}

	return nil
}
