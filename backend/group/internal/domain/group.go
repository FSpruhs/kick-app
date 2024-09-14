package domain

import (
	"errors"
	"fmt"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

var (
	ErrUserNotInGroup           = errors.New("user is not in group")
	ErrUserNotInvitedInGroup    = errors.New("user is not invited in group")
	ErrInvitingPlayerRoleTooLow = errors.New("inviting player role is too low")
)

const GroupAggregate = "group.GroupAggregate"

type Group struct {
	ddd.Aggregate
	Name           *Name
	Players        []*Player
	InvitedUserIDs []string
	InviteLevel    Role
}

func NewGroup(id string) *Group {
	return &Group{
		Aggregate: ddd.NewAggregate(id, GroupAggregate),
	}
}

func (g *Group) UserIDs() []string {
	userIDs := make([]string, len(g.Players))
	for i, p := range g.Players {
		userIDs[i] = p.UserID()
	}

	return userIDs
}

func CreateNewGroup(userID, name string) (*Group, error) {
	newName, err := NewName(name)
	if err != nil {
		return nil, fmt.Errorf("create name: %w", err)
	}

	newGroup := &Group{
		Aggregate:      ddd.NewAggregate(uuid.New().String(), GroupAggregate),
		Players:        []*Player{NewPlayer(userID, Active, Master)},
		Name:           newName,
		InvitedUserIDs: make([]string, 0),
		InviteLevel:    Admin,
	}

	newGroup.AddEvent(grouppb.GroupCreatedEvent, grouppb.GroupCreated{
		GroupID: newGroup.ID(),
		UserIDs: newGroup.UserIDs(),
	})

	return newGroup, nil
}

func (g *Group) findPlayerByUserID(userID string) (*Player, error) {
	for _, p := range g.Players {
		if p.UserID() == userID {
			return p, nil
		}
	}

	return nil, ErrUserNotInGroup
}

func (g *Group) InviteUser(invitedUserID, invitingUserID string) error {
	invitingPlayer, err := g.findPlayerByUserID(invitingUserID)
	if err != nil {
		return err
	}

	if invitingPlayer.Role() < g.InviteLevel {
		return ErrInvitingPlayerRoleTooLow
	}

	g.InvitedUserIDs = append(g.InvitedUserIDs, invitedUserID)
	g.AddEvent(grouppb.UserInvitedEvent, grouppb.UserInvited{
		GroupID:   g.ID(),
		GroupName: g.Name.Value(),
		UserID:    invitedUserID,
	})

	return nil
}

func (g *Group) HandleInvitedUserResponse(userID string, accept bool) error {
	if !contains(g.InvitedUserIDs, userID) {
		return ErrUserNotInvitedInGroup
	}

	if accept {
		g.Players = append(g.Players, NewPlayer(userID, Active, Member))
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

func removePlayer(players []*Player, userID string) []*Player {
	for i, v := range players {
		if v.UserID() == userID {
			return append(players[:i], players[i+1:]...)
		}
	}

	return players
}

func (g *Group) UserLeavesGroup(userID string) error {
	if !contains(g.UserIDs(), userID) {
		return ErrUserNotInGroup
	}

	g.Players = removePlayer(g.Players, userID)

	return nil
}
