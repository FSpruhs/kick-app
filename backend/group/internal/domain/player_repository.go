package domain

type PlayerRepository interface {
	ConfirmPlayer(playerID, groupID string, inviteLevel int) error
}
