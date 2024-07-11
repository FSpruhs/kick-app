package application

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

type App interface {
	Commands
	Queries
}

type Commands interface{}

type Queries interface{}

type Application struct{ appCommands }

type appCommands struct{}

var _ App = (*Application)(nil)

func New(players domain.PlayerRepository) *Application {
	return &Application{
		appCommands: appCommands{},
	}
}
