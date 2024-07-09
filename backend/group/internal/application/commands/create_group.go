package commands

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type CreateGroup struct {
	Name   string
	UserId string
}

type CreateGroupHandler struct {
	domain.GroupRepository
	ddd.EventPublisher
}

func NewCreateGroupHandler(groups domain.GroupRepository, eventPublisher ddd.EventPublisher) CreateGroupHandler {
	return CreateGroupHandler{groups, eventPublisher}
}

func (h CreateGroupHandler) CreateGroup(cmd *CreateGroup) (*domain.Group, error) {
	newGroup := domain.Group{
		Name:  cmd.Name,
		Users: []string{cmd.UserId},
	}
	result, err := h.GroupRepository.Create(&newGroup)
	if err != nil {
		return nil, err
	}

	if err := h.Publish(grouppb.GroupCreated{Group: result}); err != nil {
		return nil, err
	}
	return result, nil
}
