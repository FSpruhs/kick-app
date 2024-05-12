package player

import (
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) {
	players := mongodb.NewPlayerRepository(mono.DB(), "players.players")

	rest.PlayerRoutes(mono.Router(), players)

	var _ application.App
	_ = application.New(players)
}
