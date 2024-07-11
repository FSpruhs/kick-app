package commands

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type CreateGroup struct {
	Name   string
	UserId string
}

type CreateGroupHandler struct {
	domain.GroupRepository
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewCreateGroupHandler(groups domain.GroupRepository, eventPublisher ddd.EventPublisher[ddd.AggregateEvent]) CreateGroupHandler {
	return CreateGroupHandler{groups, eventPublisher}
}

func (h CreateGroupHandler) CreateGroup(cmd *CreateGroup) (*domain.Group, error) {
	newGroup := domain.CreateNewGroup(cmd.UserId, cmd.Name)

	if err := h.GroupRepository.Save(newGroup); err != nil {
		return nil, err
	}

	if err := h.Publish(newGroup.Events()...); err != nil {
		return nil, err
	}
	return newGroup, nil
}
