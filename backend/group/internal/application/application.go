package application

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	CreateGroup(cmd *commands.CreateGroup) (*domain.Group, error)
	InviteUser(cmd *commands.InviteUser) error
}

type Queries interface{}

type Application struct{ appCommands }

type appCommands struct {
	commands.CreateGroupHandler
	commands.InviteUserHandler
}

var _ App = (*Application)(nil)

func New(groups domain.GroupRepository, eventPublisher ddd.EventPublisher[ddd.AggregateEvent]) *Application {
	return &Application{
		appCommands: appCommands{
			CreateGroupHandler: commands.NewCreateGroupHandler(groups, eventPublisher),
			InviteUserHandler:  commands.NewInviteUserHandler(groups, eventPublisher),
		},
	}
}
