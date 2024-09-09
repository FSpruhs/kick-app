package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type ConfirmGroupLeavingUser struct {
	UserID  string
	GroupID string
}

type ConfirmGroupLeavingUserHandler struct {
	domain.PlayerRepository
}

func NewConfirmGroupLeavingUserHandler(players domain.PlayerRepository) ConfirmGroupLeavingUserHandler {
	return ConfirmGroupLeavingUserHandler{players}
}

func (h ConfirmGroupLeavingUserHandler) ConfirmGroupLeavingUser(cmd *ConfirmGroupLeavingUser) error {
	player, err := h.PlayerRepository.FindByUserIDAndGroupID(cmd.UserID, cmd.GroupID)
	if err != nil {
		return fmt.Errorf("player not found err: %w", err)
	}

	if player.Role == domain.Master {
		return fmt.Errorf("master cannot leave group: %w", err)
	}

	return nil
}
