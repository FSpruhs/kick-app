package group

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
)

type Module struct {
}

func (m *Module) Startup(mono monolith.Monolith) {
	groups := mongodb.NewGroupRepository(mono.DB(), "group.groups")

	var app application.App
	app = application.New(groups, mono.EventDispatcher())

	rest.GroupRouter(mono.Router(), app)
}
