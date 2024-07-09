package domain

type GroupRepository interface {
	Create(group *Group) (*Group, error)
}
