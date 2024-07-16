package commands

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type CreateGroup struct {
	Name   string
	UserID string
}

type CreateGroupHandler struct {
	domain.GroupRepository
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewCreateGroupHandler(
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) CreateGroupHandler {
	return CreateGroupHandler{groups, eventPublisher}
}

func (h CreateGroupHandler) CreateGroup(cmd *CreateGroup) (*domain.Group, error) {
	newGroup := domain.CreateNewGroup(cmd.UserID, cmd.Name)

	result, err := h.GroupRepository.Create(newGroup)
	if err != nil {
		return nil, fmt.Errorf("failed to create group: %w", err)
	}

	if err := h.Publish(newGroup.Events()...); err != nil {
		return nil, fmt.Errorf("failed to publish events: %w", err)
	}

	return result, nil
}
