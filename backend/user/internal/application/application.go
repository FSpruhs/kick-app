package application

import (
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
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

type Queries interface{}

type Application struct{ appCommands }

type appCommands struct {
	commands.CreateUserHandler
	commands.LoginUserHandler
	commands.MessageReadHandler
}

var _ App = (*Application)(nil)

func New(users domain.UserRepository, messages domain.MessageRepository) *Application {
	return &Application{
		appCommands: appCommands{
			CreateUserHandler:  commands.NewCreateUserHandler(users),
			LoginUserHandler:   commands.NewLoginUserHandler(users),
			MessageReadHandler: commands.NewMessageReadHandler(messages),
		},
	}
}
