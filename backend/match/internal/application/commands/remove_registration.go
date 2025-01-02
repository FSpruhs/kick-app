package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type RemoveRegistration struct {
	UserID         string
	MatchID        string
	RemovingUserID string
}

type RemoveRegistrationHandler struct {
	matches domain.MatchRepository
	groups  domain.GroupRepository
}

func NewRemoveRegistrationHandler(matches domain.MatchRepository, groups domain.GroupRepository) RemoveRegistrationHandler {
	return RemoveRegistrationHandler{matches, groups}
}

func (h RemoveRegistrationHandler) RemoveRegistration(cmd *RemoveRegistration) error {
	match, err := h.matches.FindByID(cmd.MatchID)
	if err != nil {
		return fmt.Errorf("getting match: %w", err)
	}

	result, err := h.groups.HasPlayerAdminRole(cmd.RemovingUserID, match.GroupID())
	if err != nil {
		return fmt.Errorf("checking if player has admin role: %w", err)
	}

	if !result {
		return fmt.Errorf("player does not have admin role")
	}

	match.RemoveRegistration(cmd.UserID)

	if err := h.matches.Save(match); err != nil {
		return fmt.Errorf("saving match: %w", err)
	}

	return nil
}
