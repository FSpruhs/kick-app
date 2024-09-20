package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type UpdatePlayer struct {
	GroupID        string
	UpdatingUserID string
	UpdatedUserID  string
	NewRole        domain.Role
	NewStatus      domain.Status
}

type UpdatePlayerHandler struct {
	groups domain.GroupRepository
}

func NewUpdatePlayerHandler(groups domain.GroupRepository) UpdatePlayerHandler {
	return UpdatePlayerHandler{groups}
}

func (h UpdatePlayerHandler) UpdatePlayer(command *UpdatePlayer) error {
	group, err := h.groups.FindByID(command.GroupID)
	if err != nil {
		return fmt.Errorf("updating player: %w", err)
	}

	if err := group.UpdatePlayer(
		command.UpdatingUserID,
		command.UpdatedUserID,
		command.NewRole,
		command.NewStatus,
	); err != nil {
		return fmt.Errorf("updating player role: %w", err)
	}

	if err := h.groups.Save(group); err != nil {
		return fmt.Errorf("updating player: %w", err)
	}

	return nil
}
