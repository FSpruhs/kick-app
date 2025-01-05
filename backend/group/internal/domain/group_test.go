package domain

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
)

func TestNewGroup(t *testing.T) {
	groupID := "test-group"
	name, _ := NewName("test-group")
	group := NewGroup(groupID, []*Player{}, name, []string{}, Master)

	assert.Equal(t, groupID, group.ID())
}

func TestUserIDs(t *testing.T) {
	userID1 := "test-user-1"
	userID2 := "test-user-2"

	group, _ := CreateNewGroup("", "test-group")
	user1 := NewPlayer(userID1, 0, 0)
	user2 := NewPlayer(userID2, 0, 0)
	group.players = []*Player{user1, user2}

	userIDs := group.UserIDs()

	assert.Equal(t, 2, len(userIDs))
	assert.ElementsMatchf(t, []string{userID1, userID2}, userIDs, "Expected user IDs to match")
}

func TestCreateNewGroup(t *testing.T) {
	userID := "test-user"
	groupName := "test-group"

	group, err := CreateNewGroup(userID, groupName)

	assert.NoError(t, err)
	assert.Equal(t, groupName, group.Name().Value())
	assert.Equal(t, 1, len(group.Players()))
	assert.Equal(t, userID, group.Players()[0].UserID())
	assert.Equal(t, 0, len(group.InvitedUserIDs()))
	assert.Equal(t, Role(Admin), group.InviteLevel())
	assert.Equal(t, Role(Master), group.Players()[0].Role())
	assert.Equal(t, Status(Active), group.Players()[0].Status())
	assert.Equal(t, 1, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[0].Payload().(grouppb.GroupCreated).GroupID)
	assert.Equal(t, userID, group.Events()[0].Payload().(grouppb.GroupCreated).UserIDs[0])
}

func TestInviteUser(t *testing.T) {
	userID := "test-user"
	invitedUserID := "invited-user"

	group, _ := CreateNewGroup(userID, "test-group")

	err := group.InviteUser(invitedUserID, userID)

	assert.NoError(t, err)
	assert.Equal(t, 1, len(group.InvitedUserIDs()))
	assert.Equal(t, invitedUserID, group.InvitedUserIDs()[0])
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserInvited).GroupID)
	assert.Equal(t, invitedUserID, group.Events()[1].Payload().(grouppb.UserInvited).UserID)
}

func TestInviteUser_UserAlreadyInvited(t *testing.T) {
	userID := "test-user"
	invitedUserID := "invited-user"

	group, _ := CreateNewGroup(userID, "test-group")
	group.invitedUserIDs = []string{invitedUserID}

	err := group.InviteUser(invitedUserID, userID)

	assert.Error(t, err)
	assert.Equal(t, ErrUserAlreadyInvited, err)
}

func TestInviteUser_UserNotInGroup(t *testing.T) {
	userID := "test-user"
	invitedUserID := "invited-user"

	group, _ := CreateNewGroup(userID, "test-group")

	err := group.InviteUser("not in group", invitedUserID)

	assert.Error(t, err)
	assert.Equal(t, ErrUserNotInGroup, err)
}

func TestInviteUser_InvitingPlayerRoleTooLow(t *testing.T) {
	userID := "test-user"
	invitedUserID := "invited-user"

	group, _ := CreateNewGroup("", "test-group")
	group.players = append(group.Players(), NewPlayer(userID, 0, 0))

	err := group.InviteUser(invitedUserID, userID)

	assert.Error(t, err)
	assert.Equal(t, ErrInvitingPlayerRoleTooLow, err)
}

func TestHandleInvitedUserResponse(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")
	group.invitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, true)

	assert.NoError(t, err)
	assert.Equal(t, 0, len(group.InvitedUserIDs()))
	assert.Equal(t, 2, len(group.Players()))
	assert.Equal(t, userID, group.Players()[1].UserID())
	assert.Equal(t, Status(Active), group.Players()[1].Status())
	assert.Equal(t, Role(Member), group.Players()[1].Role())
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).GroupID)
	assert.Equal(t, userID, group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).UserID)
}

func TestHandleInvitedUserResponse_UserNotInvitedInGroup(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")

	err := group.HandleInvitedUserResponse(userID, true)

	assert.Error(t, err)
	assert.Equal(t, ErrUserNotInvitedInGroup, err)
}

func TestHandleInvitedUserResponse_UserAlreadyInGroup(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")
	group.players = append(group.Players(), NewPlayer(userID, Leaved, Member))
	group.invitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, true)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(group.Players()))
	assert.Equal(t, userID, group.Players()[1].UserID())
	assert.Equal(t, Status(Active), group.Players()[1].Status())
	assert.Equal(t, Role(Member), group.Players()[1].Role())
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).GroupID)
	assert.Equal(t, userID, group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).UserID)
}

func TestHandleInvitedUserResponse_DeclineInvitation(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")
	group.invitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, false)

	assert.NoError(t, err)
	assert.Equal(t, 0, len(group.InvitedUserIDs()))
	assert.Equal(t, 1, len(group.Players()))
	assert.Equal(t, 1, len(group.Events()))
}

func TestUpdatePlayer(t *testing.T) {
	tests := []struct {
		updatingUserID string
		updatedUserID  string
		updatingRole   Role
		updatedRole    Role
		updatingStatus Status
		updatedStatus  Status
		newStatus      Status
		newRole        Role
		expectedErr    error
	}{
		{"1", "2", Admin, Member, Active, Active, Active, Member, nil},
		{"1", "2", Admin, Member, Active, Active, Inactive, Member, nil},
		{"1", "2", Admin, Member, Active, Active, Active, Admin, nil},
		{"1", "2", Master, Admin, Active, Active, Active, Member, nil},
		{"1", "1", Admin, Member, Active, Active, Active, Member, ErrUserCanNotSelfUpgrade},
		{"1", "2", Member, Member, Active, Active, Active, Admin, ErrMemberCanNotUpdate},
		{"1", "2", Admin, Admin, Active, Active, Active, Member, ErrOnlyMasterCanDownGradeRoleToMember},
		{"1", "2", Admin, Member, Active, Active, Active, Master, ErrOnlyMasterCanUpdateToMaster},
		{"1", "2", Master, Member, Active, Removed, Active, Master, ErrInvalidStatus},
		{"1", "2", Admin, Master, Active, Removed, Active, Admin, ErrMasterCanNotDowngradeOtherMaster},
		{"1", "2", Admin, Member, Active, Active, Removed, Member, ErrInvalidStatus},
		{"1", "2", Admin, Member, Active, Active, Leaved, Member, ErrInvalidStatus},
		{"1", "2", Admin, Member, Active, Active, NotFound, Member, ErrInvalidStatus},
		{"1", "2", Admin, Member, Active, Leaved, Inactive, Member, ErrInvalidStatus},
		{"1", "2", Admin, Member, Leaved, Active, Inactive, Member, ErrInvalidStatus},
		{"1", "2", Admin, Member, Leaved, Active, Active, Admin, ErrInvalidStatus},
		{"1", "2", Admin, Master, Active, Active, Inactive, Master, ErrMasterStatusIsAlwaysActive},
		{"1", "2", Member, Member, Active, Active, Inactive, Member, ErrMemberCanNotUpdate},
		{"3", "2", Admin, Member, Active, Active, Active, Member, ErrUserNotInGroup},
		{"1", "3", Admin, Member, Active, Active, Active, Member, ErrUserNotInGroup},
	}

	for i, test := range tests {
		t.Run(fmt.Sprintf("Test: %d", i), func(t *testing.T) {
			group, _ := CreateNewGroup("", "test-group")
			updatingPlayer := NewPlayer("1", test.updatingStatus, test.updatingRole)
			updatedPlayer := NewPlayer("2", test.updatedStatus, test.updatedRole)
			group.players = []*Player{updatingPlayer, updatedPlayer}
			err := group.UpdatePlayer(test.updatingUserID, test.updatedUserID, test.newRole, test.newStatus)

			assert.Equal(t, test.expectedErr, err)

			if err == nil {
				assert.Equal(t, test.newRole, updatedPlayer.Role())
				assert.Equal(t, test.newStatus, updatedPlayer.Status())
			}
		})

	}
}

func TestUpdatePlayer_NewMaster(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	updatedPlayer := NewPlayer("2", Active, Member)
	group.players = append(group.Players(), updatedPlayer)

	err := group.UpdatePlayer("1", "2", Master, Active)

	assert.NoError(t, err)
	assert.Equal(t, Role(Master), updatedPlayer.Role())
	assert.Equal(t, Role(Admin), group.Players()[0].Role())
}

func TestUpdatePlayer_UserSetSelfToInactive(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	updatedPlayer := NewPlayer("2", Active, Member)
	group.players = append(group.Players(), updatedPlayer)

	err := group.UpdatePlayer("2", "2", Member, Inactive)

	assert.NoError(t, err)
	assert.Equal(t, Status(Inactive), updatedPlayer.Status())
}

func TestUserLeavesGroup_Success(t *testing.T) {
	leavingUserID := "2"
	group, _ := CreateNewGroup("1", "test-group")
	leavingPlayer := NewPlayer(leavingUserID, Active, Member)
	group.players = append(group.Players(), leavingPlayer)

	err := group.UserLeavesGroup(leavingUserID)

	assert.NoError(t, err)
	assert.Equal(t, Status(Leaved), leavingPlayer.Status())
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserLeavesGroup).GroupID)
	assert.Equal(t, leavingUserID, group.Events()[1].Payload().(grouppb.UserLeavesGroup).UserID)
}

func TestUserLeavesGroup_UserNotInGroup(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")

	err := group.UserLeavesGroup("2")

	assert.Error(t, err)
	assert.Equal(t, ErrUserNotInGroup, err)
}

func TestUserLeavesGroup_MasterCanNotLeaveGroup(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	master := NewPlayer("1", Active, Master)
	group.players = append(group.Players(), master)

	err := group.UserLeavesGroup("1")

	assert.Error(t, err)
	assert.Equal(t, ErrMasterCanNotLeaveGroup, err)
}

func TestUserLeavesGroup_ErrWhenNotParticipatesInGroup(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	player := NewPlayer("2", Removed, Member)
	group.players = append(group.Players(), player)

	err := group.UserLeavesGroup("2")

	assert.Error(t, err)
	assert.Equal(t, ErrInvalidStatusForLeavingGroup, err)
}

func TestIsUserParticipateInTheGroup(t *testing.T) {
	tests := []struct {
		status   Status
		expected bool
	}{
		{Active, true},
		{Inactive, true},
		{Removed, false},
		{Leaved, false},
		{NotFound, false},
	}

	for _, test := range tests {
		t.Run(fmt.Sprintf("TestIsUserParticipateInTheGroup: %s", test.status), func(t *testing.T) {
			group, _ := CreateNewGroup("1", "test-group")
			player := NewPlayer("2", test.status, Member)
			group.players = append(group.Players(), player)

			assert.Equal(t, test.expected, group.IsUserParticipateInTheGroup("2"))
		})
	}
}

func TestRemovePlayer_Success(t *testing.T) {
	tests := []struct {
		expectedErr  error
		removeRole   Role
		removingRole Role
	}{
		{nil, Member, Admin},
		{ErrMemberCanNotUpdate, Member, Member},
		{nil, Member, Master},
		{ErrMemberCanNotUpdate, Admin, Admin},
		{ErrMemberCanNotUpdate, Admin, Member},
		{nil, Admin, Master},
		{ErrMemberCanNotUpdate, Master, Admin},
		{ErrMemberCanNotUpdate, Master, Member},
		{ErrMemberCanNotUpdate, Master, Master},
	}

	for _, test := range tests {
		t.Run(fmt.Sprintf("TestRemovePlayer: %s from Player: %s", test.removeRole, test.removingRole), func(t *testing.T) {
			group, _ := CreateNewGroup("1", "test-group")
			removePlayer := NewPlayer("2", Active, test.removeRole)
			removingPlayer := NewPlayer("3", Active, test.removingRole)

			group.players = append(group.Players(), removePlayer)
			group.players = append(group.Players(), removingPlayer)

			err := group.RemovePlayer("2", "3")

			if test.expectedErr == nil {
				assert.NoError(t, err)
				assert.Equal(t, Status(Removed), removePlayer.Status())
				assert.Equal(t, 2, len(group.Events()))
				assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.PlayerRemovedFromGroup).GroupID)
				assert.Equal(t, "2", group.Events()[1].Payload().(grouppb.PlayerRemovedFromGroup).UserID)
			} else {
				assert.Error(t, err)
				assert.Equal(t, test.expectedErr, err)
			}

		})
	}
}

func TestRemovePlayer_RemoveUserNotInGroup(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")

	err := group.RemovePlayer("2", "1")

	assert.Error(t, err)
	assert.Equal(t, ErrUserNotInGroup, err)
}

func TestRemovePlayer_RemovingUserNotInGroup(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")

	err := group.RemovePlayer("1", "2")

	assert.Error(t, err)
	assert.Equal(t, ErrUserNotInGroup, err)
}
