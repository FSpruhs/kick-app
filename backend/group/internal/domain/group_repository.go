package domain

type GroupRepository interface {
	FindByID(id string) (*Group, error)
	Save(group *Group) error
	Create(newGroup *Group) (*Group, error)
}
