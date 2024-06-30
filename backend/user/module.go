package user

import (
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest"
)

type Module struct {
}

func (m *Module) Startup(mono monolith.Monolith) {
	users := mongodb.NewUserRepository(mono.DB(), "users.users")

	var app application.App
	app = application.New(users)

	rest.UserRoutes(mono.Router(), app)
}
