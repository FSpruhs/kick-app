package domain

type GroupRepository interface {
	IsPlayerActive(userID, groupID string) (bool, error)
	HasPlayerAdminRole(userID, groupID string) (bool, error)
}
