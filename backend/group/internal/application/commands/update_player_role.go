package commands

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type UpdatePlayerRole struct {
	GroupID        string
	UpdatingUserID string
	UpdatedUserID  string
	NewRole        domain.Role
}

type UpdatePlayerRoleHandler struct {
	groups domain.GroupRepository
}

func NewUpdatePlayerRoleHandler(groups domain.GroupRepository) UpdatePlayerRoleHandler {
	return UpdatePlayerRoleHandler{groups}
}

func (h UpdatePlayerRoleHandler) UpdatePlayerRole(command *UpdatePlayerRole) error {
	group, err := h.groups.FindByID(command.GroupID)
	if err != nil {
		return fmt.Errorf("updating player role: %w", err)
	}

	if err := group.UpdatePlayerRole(command.UpdatingUserID, command.UpdatedUserID, command.NewRole); err != nil {
		return fmt.Errorf("updating player role: %w", err)
	}

	if err := h.groups.Save(group); err != nil {
		return fmt.Errorf("updating player role: %w", err)
	}

	return nil
}
