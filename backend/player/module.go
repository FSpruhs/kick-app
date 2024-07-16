package player

import (
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/grpc"
	"github.com/FSpruhs/kick-app/backend/player/internal/handler"
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest"
	"log"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	players := mongodb.NewPlayerRepository(mono.DB(), "player.players")

	app := application.New(players)

	groupEventHandler := application.NewGroupHandler(players)

	handler.RegisterGroupHandler(groupEventHandler, mono.EventDispatcher())
	rest.PlayerRoutes(mono.Router(), app)

	if err := grpc.RegisterServer(app, mono.RPC()); err != nil {
		log.Fatalf("failed to register server: %v", err)
		return err
	}

	return nil
}
