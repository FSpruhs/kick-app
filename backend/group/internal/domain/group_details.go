package domain

type GroupDetails struct {
	id          string
	groupName   *Name
	users       []*User
	inviteLevel string
}

func NewGroupDetails(group *Group, users []*User) *GroupDetails {
	return &GroupDetails{
		id:          group.ID(),
		groupName:   group.Name(),
		users:       users,
		inviteLevel: group.InviteLevel().String(),
	}
}

func (g GroupDetails) ID() string {
	return g.id
}

func (g GroupDetails) Name() string {
	return g.groupName.Value()
}

func (g GroupDetails) Users() []*User {
	return g.users
}

func (g GroupDetails) InviteLevel() string {
	return g.inviteLevel
}
