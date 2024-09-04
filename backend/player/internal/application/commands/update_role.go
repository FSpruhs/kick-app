package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type UpdateRole struct {
	PlayerToUpdateID string
	UpdatingPlayerID string
	NewRole          domain.PlayerRole
}

type UpdateRoleHandler struct {
	domain.PlayerRepository
}

func NewUpdateRoleHandler(
	players domain.PlayerRepository,
) UpdateRoleHandler {
	return UpdateRoleHandler{players}
}

func (h UpdateRoleHandler) UpdateRole(cmd *UpdateRole) error {
	playerToUpdate, err := h.PlayerRepository.FindByID(cmd.PlayerToUpdateID)
	if err != nil {
		return fmt.Errorf("searching player with id %s: %w", cmd.PlayerToUpdateID, err)
	}

	updatingPlayer, err := h.PlayerRepository.FindByID(cmd.UpdatingPlayerID)
	if err != nil {
		return fmt.Errorf("searching player with id %s: %w", cmd.PlayerToUpdateID, err)
	}

	if err := playerToUpdate.UpdateRole(updatingPlayer, cmd.NewRole); err != nil {
		return fmt.Errorf("updating role from player %s: %w", playerToUpdate.ID(), err)
	}

	playersToSave := []*domain.Player{playerToUpdate, updatingPlayer}
	if err := h.PlayerRepository.SaveAll(playersToSave); err != nil {
		return fmt.Errorf("saving player %s: %w", playerToUpdate.ID(), err)
	}

	return nil
}
