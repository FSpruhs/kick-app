package commands

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

func TestInviteUserHandler_InviteUser(t *testing.T) {

	groupID := "123"
	invitedUserID := "456"
	invitingUserID := "789"

	someErr := errors.New("some error")

	t.Run("Invite User Success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createFoundGroup(groupID, invitingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewInviteUserHandler(mockGroupRepo, mockEventRepo)

		cmd := &InviteUser{
			GroupID:        groupID,
			InvitedUserID:  invitedUserID,
			InvitingUserID: invitingUserID,
		}

		err := handler.InviteUser(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", inviteMatcher(invitedUserID))
		mockEventRepo.AssertCalled(t, "Publish", inviteEventMatcher(groupID, invitedUserID))

		mockGroupRepo.AssertExpectations(t)
		mockEventRepo.AssertExpectations(t)
	})

	t.Run("Invite User could not save group", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createFoundGroup(groupID, invitingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInviteUserHandler(mockGroupRepo, mockEventRepo)

		cmd := &InviteUser{
			GroupID:        groupID,
			InvitedUserID:  invitedUserID,
			InvitingUserID: invitingUserID,
		}

		err := handler.InviteUser(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invite User could not find group", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(nil, someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInviteUserHandler(mockGroupRepo, mockEventRepo)

		cmd := &InviteUser{
			GroupID:        groupID,
			InvitedUserID:  invitedUserID,
			InvitingUserID: invitingUserID,
		}

		err := handler.InviteUser(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invite User event error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createFoundGroup(groupID, invitingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(someErr)

		handler := NewInviteUserHandler(mockGroupRepo, mockEventRepo)

		cmd := &InviteUser{
			GroupID:        groupID,
			InvitedUserID:  invitedUserID,
			InvitingUserID: invitingUserID,
		}

		err := handler.InviteUser(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertExpectations(t)
		mockEventRepo.AssertExpectations(t)
	})

	t.Run("Invite User domain error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createFoundGroup(groupID, invitingUserID), nil)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInviteUserHandler(mockGroupRepo, mockEventRepo)

		cmd := &InviteUser{
			GroupID:        groupID,
			InvitedUserID:  invitedUserID,
			InvitingUserID: "user not in group",
		}

		err := handler.InviteUser(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertExpectations(t)
	})
}

func inviteMatcher(invitedUserID string) interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.InvitedUserIDs()) == 1 && group.InvitedUserIDs()[0] == invitedUserID
	})
}

func inviteEventMatcher(groupID, userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.UserInvited)
		return ok && event.GroupID == groupID && event.UserID == userID
	})
}

func createFoundGroup(groupID, invitingUserID string) *domain.Group {
	name, _ := domain.NewName("Group Name")

	return domain.NewGroup(
		groupID,
		[]*domain.Player{domain.NewPlayer(invitingUserID, domain.Active, domain.Master)},
		name,
		make([]string, 0),
		domain.Admin,
	)
}
