package domain

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestNewGroup(t *testing.T) {
	groupID := "test-group"
	group := NewGroup(groupID)

	assert.Equal(t, groupID, group.ID())
}

func TestUserIDs(t *testing.T) {
	userID1 := "test-user-1"
	userID2 := "test-user-2"

	group := NewGroup("test-group")
	user1 := NewPlayer(userID1, 0, 0)
	user2 := NewPlayer(userID2, 0, 0)
	group.Players = []*Player{user1, user2}

	userIDs := group.UserIDs()

	assert.Equal(t, 2, len(userIDs))
	assert.ElementsMatchf(t, []string{userID1, userID2}, userIDs, "Expected user IDs to match")
}

func TestCreateNewGroup(t *testing.T) {
	userID := "test-user"
	groupName := "test-group"

	group, err := CreateNewGroup(userID, groupName)

	assert.NoError(t, err)
	assert.Equal(t, groupName, group.Name.Value())
	assert.Equal(t, 1, len(group.Players))
	assert.Equal(t, userID, group.Players[0].UserID())
	assert.Equal(t, 0, len(group.InvitedUserIDs))
	assert.Equal(t, Role(Admin), group.InviteLevel)
	assert.Equal(t, Role(Master), group.Players[0].Role())
	assert.Equal(t, Status(Active), group.Players[0].Status())
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
	assert.Equal(t, 1, len(group.InvitedUserIDs))
	assert.Equal(t, invitedUserID, group.InvitedUserIDs[0])
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserInvited).GroupID)
	assert.Equal(t, invitedUserID, group.Events()[1].Payload().(grouppb.UserInvited).UserID)
}

func TestInviteUser_UserAlreadyInvited(t *testing.T) {
	userID := "test-user"
	invitedUserID := "invited-user"

	group, _ := CreateNewGroup(userID, "test-group")
	group.InvitedUserIDs = []string{invitedUserID}

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
	group.Players = append(group.Players, NewPlayer(userID, 0, 0))

	err := group.InviteUser(invitedUserID, userID)

	assert.Error(t, err)
	assert.Equal(t, ErrInvitingPlayerRoleTooLow, err)
}

func TestHandleInvitedUserResponse(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")
	group.InvitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, true)

	assert.NoError(t, err)
	assert.Equal(t, 0, len(group.InvitedUserIDs))
	assert.Equal(t, 2, len(group.Players))
	assert.Equal(t, userID, group.Players[1].UserID())
	assert.Equal(t, Status(Active), group.Players[1].Status())
	assert.Equal(t, Role(Member), group.Players[1].Role())
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
	group.Players = append(group.Players, NewPlayer(userID, Leaved, Member))
	group.InvitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, true)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(group.Players))
	assert.Equal(t, userID, group.Players[1].UserID())
	assert.Equal(t, Status(Active), group.Players[1].Status())
	assert.Equal(t, Role(Member), group.Players[1].Role())
	assert.Equal(t, 2, len(group.Events()))
	assert.Equal(t, group.ID(), group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).GroupID)
	assert.Equal(t, userID, group.Events()[1].Payload().(grouppb.UserAcceptedInvitation).UserID)
}

func TestHandleInvitedUserResponse_DeclineInvitation(t *testing.T) {
	userID := "test-user"
	group, _ := CreateNewGroup("", "test-group")
	group.InvitedUserIDs = []string{userID}

	err := group.HandleInvitedUserResponse(userID, false)

	assert.NoError(t, err)
	assert.Equal(t, 0, len(group.InvitedUserIDs))
	assert.Equal(t, 1, len(group.Players))
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

	for _, test := range tests {
		group, _ := CreateNewGroup("", "test-group")
		updatingPlayer := NewPlayer("1", test.updatingStatus, test.updatingRole)
		updatedPlayer := NewPlayer("2", test.updatedStatus, test.updatedRole)
		group.Players = []*Player{updatingPlayer, updatedPlayer}
		err := group.UpdatePlayer(test.updatingUserID, test.updatedUserID, test.newRole, test.newStatus)

		assert.Equal(t, test.expectedErr, err)

		if err == nil {
			assert.Equal(t, test.newRole, updatedPlayer.Role())
			assert.Equal(t, test.newStatus, updatedPlayer.Status())
		}
	}
}

func TestUpdatePlayer_NewMaster(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	updatedPlayer := NewPlayer("2", Active, Member)
	group.Players = append(group.Players, updatedPlayer)

	err := group.UpdatePlayer("1", "2", Master, Active)

	assert.NoError(t, err)
	assert.Equal(t, Role(Master), updatedPlayer.Role())
	assert.Equal(t, Role(Admin), group.Players[0].Role())
}

func TestUpdatePlayer_UserSetSelfeToInactive(t *testing.T) {
	group, _ := CreateNewGroup("1", "test-group")
	updatedPlayer := NewPlayer("2", Active, Member)
	group.Players = append(group.Players, updatedPlayer)

	err := group.UpdatePlayer("2", "2", Member, Inactive)

	assert.NoError(t, err)
	assert.Equal(t, Status(Inactive), updatedPlayer.Status())
}
