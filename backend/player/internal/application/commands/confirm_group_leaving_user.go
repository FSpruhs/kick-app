package commands

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

var ErrMasterCanNotLeaveGroup = errors.New("master cannot leave group")

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
		return fmt.Errorf("confirm group leaving user: %w", err)
	}

	if player.Role == domain.Master {
		return ErrMasterCanNotLeaveGroup
	}

	return nil
}
