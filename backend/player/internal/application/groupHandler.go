package application

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type GroupHandler struct {
	players domain.PlayerRepository
	ignoreUnimplementedDomainEvents
}

func NewGroupHandler(players domain.PlayerRepository) *GroupHandler {
	return &GroupHandler{players: players}
}

func (h GroupHandler) OnGroupCreated(event ddd.Event) error {
	orderCreated := event.(grouppb.GroupCreated)
	println(orderCreated.Group.Name)
	return nil
}
