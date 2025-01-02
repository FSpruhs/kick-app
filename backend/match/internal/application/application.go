package application

import (
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	CreateMatch(cmd *commands.CreateMatch) (*domain.Match, error)
	RespondToInvitation(cmd *commands.RespondToInvitation) error
	AddRegistration(cmd *commands.AddRegistration) error
	RemoveRegistration(cmd *commands.RemoveRegistration) error
}

type Queries interface{}

type Application struct {
	appCommands
	appQueries
}

type appCommands struct {
	commands.CreateMatchHandler
	commands.RespondToInvitationHandler
	commands.AddRegistrationHandler
	commands.RemoveRegistrationHandler
}

type appQueries struct{}

var _ App = (*Application)(nil)

func New(
	matches domain.MatchRepository,
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) *Application {
	return &Application{
		appCommands: appCommands{
			CreateMatchHandler:         commands.NewCreateMatchHandler(matches, groups, eventPublisher),
			RespondToInvitationHandler: commands.NewRespondToInvitationHandler(matches, groups),
			AddRegistrationHandler:     commands.NewAddRegistrationHandler(matches, groups),
			RemoveRegistrationHandler:  commands.NewRemoveRegistrationHandler(matches, groups),
		},
		appQueries: appQueries{},
	}
}
