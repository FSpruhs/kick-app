package grouppb

const (
	GroupCreatedEvent           = "group.GroupCreated"
	UserInvitedEvent            = "group.UserInvited"
	UserAcceptedInvitationEvent = "group.UserAcceptedInvitation"
	PlayerLeavesGroupEvent      = "group.PlayerLeavesGroup"
	PlayerRemovedFromGroupEvent = "group.PlayerRemoved"
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

type UserLeavesGroup struct {
	GroupID string
	UserID  string
}

type PlayerRemovedFromGroup struct {
	GroupID   string
	UserID    string
	GroupName string
}
