package persistence

import "github.com/FSpruhs/kick-app/backend/player/domain/player"

type PlayerRepository interface {
	Create(*player.Player) (*player.Player, error)
}
