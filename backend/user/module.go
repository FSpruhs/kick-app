package user

import (
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/handler"
	"github.com/FSpruhs/kick-app/backend/user/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	users := mongodb.NewUserRepository(mono.DB(), "user.users")
	messages := mongodb.NewMessageRepository(mono.DB(), "user.messages")

	app := application.New(users)

	groupEventHandler := application.NewGroupHandler(messages)

	handler.RegisterGroupHandler(groupEventHandler, mono.EventDispatcher())
	rest.UserRoutes(mono.Router(), app)

	return nil
}
