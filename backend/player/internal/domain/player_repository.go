package domain

type PlayerRepository interface {
	Create(player *Player) (*Player, error)
}
