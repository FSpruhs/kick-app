package domain

import (
	"errors"
	"fmt"
	"log"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

var (
	ErrMasterStatusIsAlwaysActive         = errors.New("master status is always active")
	ErrInvalidStatus                      = errors.New("invalid status for player")
	ErrInvalidStatusForLeavingGroup       = errors.New("invalid status for leaving group")
	ErrMasterCanNotLeaveGroup             = errors.New("master can not leave group")
	ErrUserAlreadyInvited                 = errors.New("user is already invited")
	ErrUserNotInGroup                     = errors.New("user is not in group")
	ErrUserNotInvitedInGroup              = errors.New("user is not invited in group")
	ErrInvitingPlayerRoleTooLow           = errors.New("inviting player role is too low")
	ErrUserCanNotSelfeUpgrade             = errors.New("user can not selfe update role")
	ErrMemberCanNotUpdate                 = errors.New("members can not update")
	ErrOnlyMasterCanDownGradeRoleToMember = errors.New("only master can downgrade role to member")
	ErrOnlyMasterCanUpdateToMaster        = errors.New("only master can update to master")
	ErrMasterCanNotDowngradeOtherMaster   = errors.New("master can not downgrade other master")
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
	if contains(g.InvitedUserIDs, invitedUserID) {
		return ErrUserAlreadyInvited
	}

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

func (g *Group) UpdatePlayer(updatingUserID, updatedUserID string, newRole Role, newStatus Status) error {
	updatingPlayer, err := g.findPlayerByUserID(updatingUserID)
	if err != nil {
		return err
	}

	updatedPlayer, err := g.findPlayerByUserID(updatedUserID)
	if err != nil {
		return err
	}

	if updatedPlayer.Role() != newRole {
		if err := updatePlayerRole(newRole, updatingPlayer, updatedPlayer); err != nil {
			return err
		}
	}

	if updatedPlayer.Status() != newStatus {
		if err := updatePlayerStatus(newStatus, updatedPlayer, updatingPlayer); err != nil {
			return err
		}
	}

	return nil
}

func updatePlayerStatus(newStatus Status, updatedPlayer, updatingPlayer *Player) error {
	if newStatus != Active && newStatus != Inactive {
		return ErrInvalidStatus
	}

	if updatedPlayer.Status() != Active && updatedPlayer.Status() != Inactive {
		return ErrInvalidStatus
	}

	if updatedPlayer.Role() == Master {
		return ErrMasterStatusIsAlwaysActive
	}

	if updatingPlayer.UserID() != updatedPlayer.UserID() && updatingPlayer.Role() == Member {
		return ErrMemberCanNotUpdate
	}

	updatedPlayer.status = newStatus

	return nil
}

func updatePlayerRole(newRole Role, updatingPlayer, updatedPlayer *Player) error {
	if updatingPlayer.UserID() == updatedPlayer.UserID() {
		return ErrUserCanNotSelfeUpgrade
	}

	if updatingPlayer.Role() == Member {
		return ErrMemberCanNotUpdate
	}

	switch newRole {
	case Member:
		if updatingPlayer.Role() != Master {
			return ErrOnlyMasterCanDownGradeRoleToMember
		}
	case Master:
		if updatingPlayer.Role() != Master {
			return ErrOnlyMasterCanUpdateToMaster
		}

		updatingPlayer.role = Admin

	case Admin:
		if updatedPlayer.Role() == Master && updatingPlayer.Role() != Master {
			return ErrMasterCanNotDowngradeOtherMaster
		}
	}

	updatedPlayer.role = newRole

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
	player, err := g.findPlayerByUserID(userID)
	if err != nil {
		return err
	}

	if player.role == Master {
		return ErrMasterCanNotLeaveGroup
	}

	if participatesInGroup(player) {
		return ErrInvalidStatusForLeavingGroup

	}

	player.status = Leaved

	return nil
}

func (g *Group) UserForPlayerNotFound(userID string) {
	player, err := g.findPlayerByUserID(userID)
	if err != nil {
		log.Printf("player not found: %s\n", userID)
	}

	player.status = NotFound
}

func (g *Group) IsUserParticipateInTheGroup(userID string) bool {
	player, err := g.findPlayerByUserID(userID)
	if err != nil {
		return false
	}

	if participatesInGroup(player) {
		return false
	}

	return true
}

func participatesInGroup(player *Player) bool {
	return player.Status() != Active && player.Status() != Inactive
}
