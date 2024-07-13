package grouppb

const GroupCreatedEvent = "group.GroupCreated"
const UserInvitedEvent = "group.UserInvited"

type GroupCreated struct {
	GroupID string
	UserIds []string
}

type UserInvited struct {
	GroupId   string
	GroupName string
	UserId    string
}
