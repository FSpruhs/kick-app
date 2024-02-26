package player

import (
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) {
	players := mongodb.NewPlayerRepository(mono.DB(), "players.players")

	var app application.App
	app = application.New(players)
}
