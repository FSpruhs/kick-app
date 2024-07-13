package domain

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/google/uuid"
)

var ErrGroupNotFound = fmt.Errorf("could not find group with given id")

const GroupAggregate = "group.GroupAggregate"

type Group struct {
	ddd.Aggregate
	Name           *Name
	UserIds        []string
	InvitedUserIds []string
}

func NewGroup(id string) *Group {
	return &Group{
		Aggregate: ddd.NewAggregate(id, GroupAggregate),
	}
}

func CreateNewGroup(userId, name string) *Group {
	newName, err := NewName(name)
	if err != nil {
		return nil
	}

	newGroup := &Group{
		Aggregate:      ddd.NewAggregate(uuid.New().String(), GroupAggregate),
		UserIds:        []string{userId},
		Name:           newName,
		InvitedUserIds: make([]string, 0),
	}
	newGroup.AddEvent(grouppb.GroupCreatedEvent, grouppb.GroupCreated{
		GroupID: newGroup.ID(),
		UserIds: newGroup.UserIds,
	})

	return newGroup
}

func (g *Group) InviteUser(userId string) {
	g.InvitedUserIds = append(g.InvitedUserIds, userId)
	g.AddEvent(grouppb.UserInvitedEvent, grouppb.UserInvited{
		GroupId:   g.ID(),
		GroupName: g.Name.Value(),
		UserId:    userId,
	})
}
