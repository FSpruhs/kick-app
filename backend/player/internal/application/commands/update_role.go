package commands

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type UpdateRole struct {
	PlayerToUpdateID string
	UpdatingPlayerID string
	NewRole          domain.PlayerRole
}

type UpdateRoleHandler struct {
	domain.PlayerRepository
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewUpdateRoleHandler(
	players domain.PlayerRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) UpdateRoleHandler {
	return UpdateRoleHandler{players, eventPublisher}
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

	if err := h.PlayerRepository.Save(playerToUpdate); err != nil {
		return fmt.Errorf("saving player %s: %w", playerToUpdate.ID(), err)
	}

	if err := h.Publish(playerToUpdate.Events()...); err != nil {
		return fmt.Errorf("publishing events from player %s: %w", playerToUpdate.ID(), err)
	}

	return nil
}
