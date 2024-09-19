package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type LeaveGroup struct {
	GroupID string
	UserID  string
}

type LeaveGroupHandler struct {
	domain.GroupRepository
	domain.PlayerRepository
}

func NewLeaveGroupHandler(groups domain.GroupRepository, players domain.PlayerRepository) LeaveGroupHandler {
	return LeaveGroupHandler{groups, players}
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

	return nil
}
