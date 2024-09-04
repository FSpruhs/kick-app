package domain

import (
	"errors"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

var ErrGroupNotFound = errors.New("could not find group with given id")

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
