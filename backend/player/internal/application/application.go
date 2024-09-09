package application

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	ConfirmPlayer(cmd *commands.ConfirmPlayer) error
	ConfirmGroupLeavingUser(cmd *commands.ConfirmGroupLeavingUser) error
	UpdateRole(cmd *commands.UpdateRole) error
}

type Queries interface{}

type Application struct{ appCommands }

type appCommands struct {
	commands.ConfirmGroupLeavingUserHandler
	commands.ConfirmPlayerHandler
	commands.UpdateRoleHandler
}

var _ App = (*Application)(nil)

func New(players domain.PlayerRepository) *Application {
	return &Application{
		appCommands: appCommands{
			ConfirmPlayerHandler:           commands.NewConfirmPlayerHandler(players),
			ConfirmGroupLeavingUserHandler: commands.NewConfirmGroupLeavingUserHandler(players),
			UpdateRoleHandler:              commands.NewUpdateRoleHandler(players),
		},
	}
}
