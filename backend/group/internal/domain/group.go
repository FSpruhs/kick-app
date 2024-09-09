package domain

import (
	"errors"
	"fmt"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

var (
	ErrGroupNotFound         = errors.New("could not find group with given id")
	ErrUserNotInvitedInGroup = errors.New("user is not invited in group")
)

const GroupAggregate = "group.GroupAggregate"

type Group struct {
	ddd.Aggregate
	Name           *Name
	UserIDs        []string
	InvitedUserIDs []string
	InviteLevel    int
}

func NewGroup(id string) *Group {
	return &Group{
		Aggregate: ddd.NewAggregate(id, GroupAggregate),
	}
}

func CreateNewGroup(userID, name string) *Group {
	newName, err := NewName(name)
	if err != nil {
		return nil
	}

	newGroup := &Group{
		Aggregate:      ddd.NewAggregate(uuid.New().String(), GroupAggregate),
		UserIDs:        []string{userID},
		Name:           newName,
		InvitedUserIDs: make([]string, 0),
		InviteLevel:    1,
	}
	newGroup.AddEvent(grouppb.GroupCreatedEvent, grouppb.GroupCreated{
		GroupID: newGroup.ID(),
		UserIDs: newGroup.UserIDs,
	})

	return newGroup
}

func (g *Group) InviteUser(userID string) {
	g.InvitedUserIDs = append(g.InvitedUserIDs, userID)
	g.AddEvent(grouppb.UserInvitedEvent, grouppb.UserInvited{
		GroupID:   g.ID(),
		GroupName: g.Name.Value(),
		UserID:    userID,
	})
}

func (g *Group) HandleInvitedUserResponse(userID string, accept bool) error {
	if !g.containsInvitedUser(userID) {
		return ErrUserNotInvitedInGroup
	}

	if accept {
		g.UserIDs = append(g.UserIDs, userID)
		g.InvitedUserIDs = g.removeInvitedUser(userID)
		g.AddEvent(grouppb.UserAcceptedInvitationEvent, grouppb.UserAcceptedInvitation{GroupID: g.ID(), UserID: userID})
	} else {
		g.InvitedUserIDs = g.removeInvitedUser(userID)
	}

	return nil
}

func (g *Group) containsInvitedUser(userID string) bool {
	for _, id := range g.InvitedUserIDs {
		if id == userID {
			return true
		}
	}

	return false
}

func (g *Group) containsUser(userID string) bool {
	for _, id := range g.UserIDs {
		if id == userID {
			return true
		}
	}

	return false
}

func (g *Group) removeInvitedUser(userID string) []string {
	for i, id := range g.InvitedUserIDs {
		if id == userID {
			return append(g.InvitedUserIDs[:i], g.InvitedUserIDs[i+1:]...)
		}
	}

	return g.InvitedUserIDs
}

func (g *Group) removeUser(userID string) []string {
	for i, id := range g.UserIDs {
		if id == userID {
			return append(g.UserIDs[:i], g.UserIDs[i+1:]...)
		}
	}

	return g.InvitedUserIDs
}

func (g *Group) UserLeavesGroup(userID string) error {
	if !g.containsUser(userID) {
		return fmt.Errorf("user %s is not in group %s", userID, g.ID())
	}

	g.UserIDs = g.removeUser(userID)

	return nil
}
