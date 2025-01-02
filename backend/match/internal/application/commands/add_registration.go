package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type AddRegistration struct {
	UserID       string
	MatchID      string
	AddingUserID string
}

type AddRegistrationHandler struct {
	domain.MatchRepository
	domain.GroupRepository
}

func NewAddRegistrationHandler(
	matches domain.MatchRepository,
	groups domain.GroupRepository,
) AddRegistrationHandler {
	return AddRegistrationHandler{matches, groups}
}

func (h AddRegistrationHandler) AddRegistration(cmd *AddRegistration) error {

	match, err := h.MatchRepository.FindByID(cmd.MatchID)
	if err != nil {
		return fmt.Errorf("finding match: %w", err)
	}

	result, err := h.HasPlayerAdminRole(cmd.AddingUserID, match.GroupID())
	if err != nil {
		return fmt.Errorf("checking if player has admin role: %w", err)
	}

	if !result {
		return fmt.Errorf("player does not have admin role")
	}

	if err := match.AddRegistration(cmd.UserID); err != nil {
		return fmt.Errorf("adding registration: %w", err)
	}

	if err := h.MatchRepository.Save(match); err != nil {
		return fmt.Errorf("saving match: %w", err)
	}

	return nil
}
