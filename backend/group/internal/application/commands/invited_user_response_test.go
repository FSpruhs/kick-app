package commands

import (
	"errors"
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"testing"
)

func TestInvitedUserResponseHandler_InvitedUserResponse(t *testing.T) {
	groupID := "123"
	userID := "456"

	someErr := errors.New("some error")

	t.Run("Invited User Response true Success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   userID,
			Accepted: true,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", inviteResponseTrueMatcher(userID))
		mockEventRepo.AssertCalled(t, "Publish", inviteResponseEventMatcher(groupID, userID))

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invited User Response false Success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   userID,
			Accepted: false,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", inviteResponseFalseMatcher())
		mockEventRepo.AssertCalled(t, "Publish", []ddd.AggregateEvent{})

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invited User Response group not found", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(nil, someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   userID,
			Accepted: true,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invited User Response save error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   userID,
			Accepted: false,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", inviteResponseFalseMatcher())

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invited User Response publish fail", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(someErr)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   userID,
			Accepted: false,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", inviteResponseFalseMatcher())
		mockEventRepo.AssertCalled(t, "Publish", []ddd.AggregateEvent{})

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Invited User Response user not invited", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createGroup(groupID, userID), nil)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewInvitedUserResponseHandler(mockGroupRepo, mockEventRepo)

		cmd := &InvitedUserResponse{
			GroupID:  groupID,
			UserID:   "not invited",
			Accepted: false,
		}

		err := handler.InvitedUserResponse(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})
}

func inviteResponseTrueMatcher(invitedUserID string) interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.InvitedUserIDs()) == 0 &&
			len(group.Players()) == 1 &&
			group.Players()[0].UserID() == invitedUserID &&
			group.Players()[0].Role() == domain.Member &&
			group.Players()[0].Status() == domain.Active
	})
}

func inviteResponseFalseMatcher() interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.InvitedUserIDs()) == 0 && len(group.Players()) == 0
	})
}

func inviteResponseEventMatcher(groupID, userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.UserAcceptedInvitation)
		return ok && event.GroupID == groupID && event.UserID == userID
	})
}

func createGroup(groupID, invitedUserID string) *domain.Group {
	name, _ := domain.NewName("Group Name")

	return domain.NewGroup(
		groupID,
		[]*domain.Player{},
		name,
		[]string{invitedUserID},
		domain.Admin,
	)
}
