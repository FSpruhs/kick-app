package application

import "github.com/FSpruhs/kick-app/backend/player/internal/domain"

type CreatePlayer struct {
	firstName string
	lastName  string
}

type Application struct {
	players domain.PlayerRepository
}

type App interface {
	CreatePlayer(create CreatePlayer)
}

func New(players domain.PlayerRepository) *Application {
	return &Application{players: players}
}

func (a Application) CreatePlayer(player CreatePlayer) {

}
