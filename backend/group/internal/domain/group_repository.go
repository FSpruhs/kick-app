package domain

type GroupRepository interface {
	FindByID(id string) (*Group, error)
	Save(group *Group) error
	Create(newGroup *Group) (*Group, error)
	FindAllByUserID(userID string) ([]*Group, error)
}
