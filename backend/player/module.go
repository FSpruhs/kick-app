package player

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/grpc"
	"github.com/FSpruhs/kick-app/backend/player/internal/handler"
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	players := mongodb.NewPlayerRepository(mono.DB(), "player.players")

	app := application.New(players)

	groupEventHandler := application.NewGroupHandler(players)

	handler.RegisterGroupHandler(groupEventHandler, mono.EventDispatcher())
	rest.PlayerRoutes(mono.Router(), app)

	if err := grpc.RegisterServer(app, mono.RPC()); err != nil {
		return fmt.Errorf("register player server: %w", err)
	}

	return nil
}
