package domain

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/google/uuid"
)

const GroupAggregate = "group.GroupAggregate"

type Group struct {
	ddd.Aggregate
	Name           string
	Users          []string
	InvitedUserIds []string
}

func NewGroup(id string) *Group {
	return &Group{
		Aggregate: ddd.NewAggregate(id, GroupAggregate),
	}
}

func CreateNewGroup(userId, name string) *Group {
	newGroup := &Group{
		Aggregate:      ddd.NewAggregate(uuid.New().String(), GroupAggregate),
		Users:          []string{userId},
		Name:           name,
		InvitedUserIds: make([]string, 0),
	}
	newGroup.AddEvent(grouppb.GroupCreatedEvent, grouppb.GroupCreated{Group: newGroup})

	return newGroup
}

func (g *Group) InviteUser(userId string) {
	g.Users = append(g.InvitedUserIds, userId)
}
