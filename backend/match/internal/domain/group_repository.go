package domain

type GroupRepository interface {
	IsPlayerActive(userID, groupID string) (bool, error)
}
