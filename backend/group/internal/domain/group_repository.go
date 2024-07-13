package domain

type GroupRepository interface {
	FindById(id string) (*Group, error)
	Save(group *Group) error
	Create(newGroup *Group) (*Group, error)
}
