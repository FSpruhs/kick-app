package grouppb

import "github.com/FSpruhs/kick-app/backend/group/internal/domain"

const GroupCreatedEvent = "group.GroupCreated"

type GroupCreated struct {
	Group *domain.Group
}

func (GroupCreated) EventName() string {
	return "group.GroupCreated"
}

type UserInvited struct {
	GroupId string
	UserId  string
}

func (UserInvited) EventName() string {
	return "group.UserInvited"
}
