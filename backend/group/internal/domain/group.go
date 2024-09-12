package domain

import (
	"errors"
	"fmt"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

var (
	ErrUserNotInGroup        = errors.New("user is not in group")
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

func CreateNewGroup(userID, name string) (*Group, error) {
	newName, err := NewName(name)
	if err != nil {
		return nil, fmt.Errorf("create name: %w", err)
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

	return newGroup, nil
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
	if !contains(g.InvitedUserIDs, userID) {
		return ErrUserNotInvitedInGroup
	}

	if accept {
		g.UserIDs = append(g.UserIDs, userID)
		g.InvitedUserIDs = remove(g.InvitedUserIDs, userID)
		g.AddEvent(grouppb.UserAcceptedInvitationEvent, grouppb.UserAcceptedInvitation{GroupID: g.ID(), UserID: userID})
	} else {
		g.InvitedUserIDs = remove(g.InvitedUserIDs, userID)
	}

	return nil
}

func contains(array []string, value string) bool {
	for _, v := range array {
		if v == value {
			return true
		}
	}

	return false
}

func remove(array []string, value string) []string {
	for i, v := range array {
		if v == value {
			return append(array[:i], array[i+1:]...)
		}
	}

	return array
}

func (g *Group) UserLeavesGroup(userID string) error {
	if !contains(g.UserIDs, userID) {
		return ErrUserNotInGroup
	}

	g.UserIDs = remove(g.UserIDs, userID)

	return nil
}
