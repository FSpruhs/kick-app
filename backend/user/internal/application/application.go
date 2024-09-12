package application

import (
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	CreateUser(cmd *commands.CreateUser) (*domain.User, error)
	LoginUser(cmd *commands.LoginUser) (*domain.User, error)
	MessageRead(cmd *commands.MessageRead) error
}

type Queries interface {
	GetUser(cmd *queries.GetUser) (*domain.User, error)
	GetUserAll(cmd *queries.GetUserAll) ([]*domain.User, error)
}

type Application struct {
	appCommands
	appQueries
}

type appCommands struct {
	commands.CreateUserHandler
	commands.LoginUserHandler
	commands.MessageReadHandler
}

type appQueries struct {
	queries.GetUserHandler
	queries.GetUserAllHandler
}

var _ App = (*Application)(nil)

func New(users domain.UserRepository, messages domain.MessageRepository) *Application {
	return &Application{
		appCommands: appCommands{
			CreateUserHandler:  commands.NewCreateUserHandler(users),
			LoginUserHandler:   commands.NewLoginUserHandler(users),
			MessageReadHandler: commands.NewMessageReadHandler(messages),
		},
		appQueries: appQueries{
			GetUserHandler:    queries.NewGetUserHandler(users),
			GetUserAllHandler: queries.NewGetUserAllHandler(users),
		},
	}
}
