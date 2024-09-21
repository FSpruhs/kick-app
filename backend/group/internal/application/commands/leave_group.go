package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type LeaveGroup struct {
	GroupID string
	UserID  string
}

type LeaveGroupHandler struct {
	domain.GroupRepository
	domain.PlayerRepository
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent]
}

func NewLeaveGroupHandler(
	groups domain.GroupRepository,
	players domain.PlayerRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) LeaveGroupHandler {
	return LeaveGroupHandler{groups, players, eventPublisher}
}

func (h LeaveGroupHandler) LeaveGroup(cmd *LeaveGroup) error {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return fmt.Errorf("user is leaving a group: %w", err)
	}

	if err := group.UserLeavesGroup(cmd.UserID); err != nil {
		return fmt.Errorf("user is leaving a group: %w", err)
	}

	if err := h.GroupRepository.Save(group); err != nil {
		return fmt.Errorf("user is leaving a group: %w", err)
	}

	if err := h.eventPublisher.Publish(group.Events()...); err != nil {
		return fmt.Errorf("publish user is leaving a group event: %w", err)
	}

	return nil
}
