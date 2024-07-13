package grouppb

const GroupCreatedEvent = "group.GroupCreated"
const UserInvitedEvent = "group.UserInvited"

type GroupCreated struct {
	GroupID string
	Users   []string
}

type UserInvited struct {
	GroupId string
	UserId  string
}
