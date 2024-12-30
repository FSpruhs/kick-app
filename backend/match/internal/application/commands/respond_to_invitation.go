package commands

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type RespondToInvitation struct {
	MatchID            string
	Accept             bool
	RespondingPlayerID string
	PlayerID           string
}

type RespondToInvitationHandler struct {
	domain.MatchRepository
	domain.GroupRepository
}

func NewRespondToInvitationHandler(matches domain.MatchRepository, groups domain.GroupRepository) RespondToInvitationHandler {
	return RespondToInvitationHandler{matches, groups}
}

func (h RespondToInvitationHandler) RespondToInvitation(cmd *RespondToInvitation) error {
	if cmd.PlayerID != cmd.RespondingPlayerID {
		return errors.New("player is not responding to the invitation")
	}

	match, err := h.MatchRepository.FindByID(cmd.MatchID)
	if err != nil {
		return fmt.Errorf("failed to find match: %w", err)
	}

	result, err := h.GroupRepository.IsPlayerActive(cmd.PlayerID, match.GroupID())
	if err != nil {
		return fmt.Errorf("failed to check if player is active: %w", err)
	}

	if !result {
		return errors.New("player is not active")
	}

	match.RespondToInvitation(cmd.PlayerID, cmd.Accept)

	if err := h.MatchRepository.Save(match); err != nil {
		return fmt.Errorf("saving match after respond to invitation: %w", err)
	}

	return nil
}
