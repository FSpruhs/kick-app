package domain

type PlayerRepository interface {
	ConfirmPlayer(playerID string) error
}
