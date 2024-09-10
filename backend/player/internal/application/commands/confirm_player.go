package commands

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

var (
	ErrPlayerNotInGroup  = errors.New("player is not in the group")
	ErrInviteLevelTooLow = errors.New("invite level is too low")
)

type ConfirmPlayer struct {
	PlayerID    string
	GroupID     string
	InviteLevel int
}

type ConfirmPlayerHandler struct {
	domain.PlayerRepository
}

func NewConfirmPlayerHandler(players domain.PlayerRepository) ConfirmPlayerHandler {
	return ConfirmPlayerHandler{players}
}

func (h ConfirmPlayerHandler) ConfirmPlayer(cmd *ConfirmPlayer) error {
	player, err := h.PlayerRepository.FindByID(cmd.PlayerID)
	if err != nil {
		return fmt.Errorf("confirm player: %w", err)
	}

	if player.GroupID != cmd.GroupID {
		return ErrPlayerNotInGroup
	}

	if player.Role < domain.PlayerRole(cmd.InviteLevel) {
		return ErrInviteLevelTooLow
	}

	return nil
}
