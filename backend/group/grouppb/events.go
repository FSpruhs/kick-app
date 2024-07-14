package grouppb

const (
	GroupCreatedEvent = "group.GroupCreated"
	UserInvitedEvent  = "group.UserInvited"
)

type GroupCreated struct {
	GroupID string
	UserIDs []string
}

type UserInvited struct {
	GroupID   string
	GroupName string
	UserID    string
}
