package grouppb

import "github.com/FSpruhs/kick-app/backend/group/internal/domain"

type GroupCreated struct {
	Group *domain.Group
}

func (GroupCreated) EventName() string {
	return "group.GroupCreated"
}
