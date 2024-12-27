package application

import (
	"github.com/FSpruhs/kick-app/backend/match/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	CreateMatch(cmd *commands.CreateMatch) (*domain.Match, error)
}

type Queries interface{}

type Application struct {
	appCommands
	appQueries
}

type appCommands struct {
	commands.CreateMatchHandler
}

type appQueries struct{}

var _ App = (*Application)(nil)

func New(matches domain.MatchRepository, groups domain.GroupRepository) *Application {
	return &Application{
		appCommands: appCommands{
			CreateMatchHandler: commands.NewCreateMatchHandler(matches, groups),
		},
		appQueries: appQueries{},
	}
}
