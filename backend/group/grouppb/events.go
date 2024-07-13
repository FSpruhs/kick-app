package grouppb

const GroupCreatedEvent = "group.GroupCreated"

type GroupCreated struct {
	GroupID string
	Users   []string
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
