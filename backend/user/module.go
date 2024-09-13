package user

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/grpc"
	"github.com/FSpruhs/kick-app/backend/user/internal/handler"
	"github.com/FSpruhs/kick-app/backend/user/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	users, err := mongodb.NewUserRepository(mono.DB(), "user.users")
	if err != nil {
		return fmt.Errorf("creating user repository: %w", err)
	}

	messages := mongodb.NewMessageRepository(mono.DB(), "user.messages")

	app := application.New(users, messages)

	groupEventHandler := application.NewGroupHandler(messages, users)

	handler.RegisterGroupHandler(groupEventHandler, mono.EventDispatcher())
	rest.UserRoutes(mono.Router(), app)

	if err := grpc.RegisterServer(app, mono.RPC()); err != nil {
		return fmt.Errorf("register user server: %w", err)
	}

	return nil
}
