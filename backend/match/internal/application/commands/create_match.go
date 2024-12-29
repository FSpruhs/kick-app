package commands

import (
	"fmt"
	"time"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
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
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewCreateMatchHandler(
	matches domain.MatchRepository,
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) CreateMatchHandler {
	return CreateMatchHandler{matches, groups, eventPublisher}
}

func (h CreateMatchHandler) CreateMatch(cmd *CreateMatch) (*domain.Match, error) {
	isPlayerActive, err := h.IsPlayerActive(cmd.UserID, cmd.GroupID)
	if err != nil || !isPlayerActive {
		return nil, fmt.Errorf("checking if player is active: %w", err)
	}

	match := domain.NewMatch(cmd.Begin, *cmd.Location, *cmd.PlayerCount, cmd.GroupID)

	if err := h.MatchRepository.Save(match); err != nil {
		return nil, fmt.Errorf("saving match: %w", err)
	}

	if err := h.EventPublisher.Publish(match.Events()...); err != nil {
		return nil, fmt.Errorf("publishing match created event: %w", err)
	}

	return match, nil
}
