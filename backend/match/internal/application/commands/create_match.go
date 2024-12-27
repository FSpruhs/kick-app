package commands

import (
	"fmt"
	"time"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type CreateMatch struct {
	UserID      string
	GroupID     string
	Begin       time.Time
	Location    *domain.Location
	PlayerCount *domain.PlayerCount
}

type CreateMatchHandler struct {
	domain.MatchRepository
	domain.GroupRepository
}

func NewCreateMatchHandler(match domain.MatchRepository, groups domain.GroupRepository) CreateMatchHandler {
	return CreateMatchHandler{match, groups}
}

func (h CreateMatchHandler) CreateMatch(cmd *CreateMatch) (*domain.Match, error) {
	isPlayerActive, err := h.IsPlayerActive(cmd.UserID, cmd.GroupID)
	if err != nil || !isPlayerActive {
		return nil, fmt.Errorf("user %s is not active in group %s", cmd.UserID, cmd.GroupID)
	}

	match := domain.NewMatch(cmd.Begin, *cmd.Location, *cmd.PlayerCount)
	return match, nil
}
