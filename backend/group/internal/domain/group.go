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
	ErrUserCanNotSelfUpgrade              = errors.New("user can not selfe update role")
	ErrMemberCanNotUpdate                 = errors.New("members can not update")
	ErrOnlyMasterCanDownGradeRoleToMember = errors.New("only master can downgrade role to member")
	ErrOnlyMasterCanUpdateToMaster        = errors.New("only master can update to master")
	ErrMasterCanNotDowngradeOtherMaster   = errors.New("master can not downgrade other master")
)

const GroupAggregate = "group.GroupAggregate"

type Group struct {
	ddd.Aggregate
	name           *Name
	players        []*Player
	invitedUserIDs []string
	inviteLevel    Role
}

func NewGroup(id string, players []*Player, name *Name, invitedUserIDs []string, inviteLevel Role) *Group {
	return &Group{
		Aggregate:      ddd.NewAggregate(id, GroupAggregate),
		players:        players,
		name:           name,
		invitedUserIDs: invitedUserIDs,
		inviteLevel:    inviteLevel,
	}
}

func CreateNewGroup(userID, name string) (*Group, error) {
	newName, err := NewName(name)
	if err != nil {
		return nil, fmt.Errorf("create name: %w", err)
	}

	newGroup := &Group{
		Aggregate:      ddd.NewAggregate(uuid.New().String(), GroupAggregate),
		players:        []*Player{NewPlayer(userID, Active, Master)},
		name:           newName,
		invitedUserIDs: make([]string, 0),
		inviteLevel:    Admin,
	}

	newGroup.AddEvent(grouppb.GroupCreatedEvent, grouppb.GroupCreated{
		GroupID: newGroup.ID(),
		UserIDs: newGroup.UserIDs(),
	})

	return newGroup, nil
}

func (g *Group) UserIDs() []string {
	userIDs := make([]string, len(g.Players()))
	for i, p := range g.Players() {
		userIDs[i] = p.UserID()
	}

	return userIDs
}

func (g *Group) InviteUser(invitedUserID, invitingUserID string) error {
	if contains(g.InvitedUserIDs(), invitedUserID) {
		return ErrUserAlreadyInvited
	}

	invitingPlayer, err := findPlayerByUserID(g.Players(), invitingUserID)
	if err != nil {
		return err
	}

	if invitingPlayer.Role() < g.InviteLevel() {
		return ErrInvitingPlayerRoleTooLow
	}

	g.invitedUserIDs = append(g.InvitedUserIDs(), invitedUserID)

	g.AddEvent(grouppb.UserInvitedEvent, grouppb.UserInvited{
		GroupID:   g.ID(),
		GroupName: g.Name().Value(),
		UserID:    invitedUserID,
	})

	return nil
}

func (g *Group) HandleInvitedUserResponse(userID string, accept bool) error {
	if !contains(g.InvitedUserIDs(), userID) {
		return ErrUserNotInvitedInGroup
	}

	if accept {
		return g.acceptInvitation(userID)
	}

	g.rejectInvitation(userID)

	return nil
}

func (g *Group) acceptInvitation(userID string) error {
	player, err := findPlayerByUserID(g.Players(), userID)
	if err != nil {
		g.players = append(g.Players(), NewPlayer(userID, Active, Member))
	} else {
		player.status = Active
	}

	g.invitedUserIDs = remove(g.InvitedUserIDs(), userID)
	g.AddEvent(grouppb.UserAcceptedInvitationEvent, grouppb.UserAcceptedInvitation{GroupID: g.ID(), UserID: userID})

	return nil
}

func (g *Group) rejectInvitation(userID string) {
	g.invitedUserIDs = remove(g.InvitedUserIDs(), userID)
}

func (g *Group) UpdatePlayer(updatingUserID, updatedUserID string, newRole Role, newStatus Status) error {
	updatingPlayer, err := findPlayerByUserID(g.Players(), updatingUserID)
	if err != nil {
		return err
	}

	updatedPlayer, err := findPlayerByUserID(g.Players(), updatedUserID)
	if err != nil {
		return err
	}

	if notParticipatesInGroup(updatingPlayer) {
		return ErrInvalidStatus
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

func (g *Group) UserLeavesGroup(userID string) error {
	player, err := findPlayerByUserID(g.Players(), userID)
	if err != nil {
		return err
	}

	if player.role == Master {
		return ErrMasterCanNotLeaveGroup
	}

	if notParticipatesInGroup(player) {
		return ErrInvalidStatusForLeavingGroup
	}

	player.status = Leaved

	g.AddEvent(grouppb.PlayerLeavesGroupEvent, grouppb.UserLeavesGroup{
		GroupID: g.ID(),
		UserID:  userID,
	})

	return nil
}

func (g *Group) UserForPlayerNotFound(userID string) {
	player, err := findPlayerByUserID(g.Players(), userID)
	if err != nil {
		log.Printf("player not found: %s\n", userID)
	}

	player.status = NotFound
}

func (g *Group) IsUserParticipateInTheGroup(userID string) bool {
	player, err := findPlayerByUserID(g.Players(), userID)
	if err != nil {
		return false
	}

	if notParticipatesInGroup(player) {
		return false
	}

	return true
}

func (g *Group) RemovePlayer(removeUserID, removingUserID string) error {
	removePlayer, err := findPlayerByUserID(g.Players(), removeUserID)
	if err != nil {
		return err
	}

	removingPlayer, err := findPlayerByUserID(g.Players(), removingUserID)
	if err != nil {
		return err
	}

	if removingPlayer.Role() <= removePlayer.Role() {
		return ErrMemberCanNotUpdate
	}

	removePlayer.status = Removed

	g.AddEvent(grouppb.PlayerRemovedFromGroupEvent, grouppb.PlayerRemovedFromGroup{
		GroupID:   g.ID(),
		UserID:    removeUserID,
		GroupName: g.Name().Value(),
	})

	return nil
}

func (g *Group) Players() []*Player {
	return g.players
}

func (g *Group) Name() *Name {
	return g.name
}

func (g *Group) InvitedUserIDs() []string {
	return g.invitedUserIDs
}

func (g *Group) InviteLevel() Role {
	return g.inviteLevel
}

func notParticipatesInGroup(player *Player) bool {
	return player.Status() != Active && player.Status() != Inactive
}

func remove(array []string, value string) []string {
	for i, v := range array {
		if v == value {
			return append(array[:i], array[i+1:]...)
		}
	}

	return array
}

func contains(array []string, value string) bool {
	for _, v := range array {
		if v == value {
			return true
		}
	}

	return false
}

func findPlayerByUserID(players []*Player, userID string) (*Player, error) {
	for _, p := range players {
		if p.UserID() == userID {
			return p, nil
		}
	}

	return nil, ErrUserNotInGroup
}

func updatePlayerRole(newRole Role, updatingPlayer, updatedPlayer *Player) error {
	if updatingPlayer.UserID() == updatedPlayer.UserID() {
		return ErrUserCanNotSelfUpgrade
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

		if notParticipatesInGroup(updatedPlayer) {
			return ErrInvalidStatus
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

func updatePlayerStatus(newStatus Status, updatedPlayer, updatingPlayer *Player) error {
	if newStatus != Active && newStatus != Inactive {
		return ErrInvalidStatus
	}

	if notParticipatesInGroup(updatedPlayer) {
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
