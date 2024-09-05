package grouppb

const (
	GroupCreatedEvent           = "group.GroupCreated"
	UserInvitedEvent            = "group.UserInvited"
	UserAcceptedInvitationEvent = "group.UserAcceptedInvitation"
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

type UserAcceptedInvitation struct {
	GroupID string
	UserID  string
}
