package domain

type GroupRepository interface {
	FindPlayersByGroup(groupID string) ([]string, error)
}
