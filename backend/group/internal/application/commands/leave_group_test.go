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

func TestLeaveGroupHandler_LeaveGroup(t *testing.T) {
	groupID := "123"
	userID := "456"

	someErr := errors.New("some error")

	t.Run("Leave Group Success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createLeaveGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewLeaveGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &LeaveGroup{
			GroupID: groupID,
			UserID:  userID,
		}

		err := handler.LeaveGroup(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", leaveGroupMatcher())
		mockEventRepo.AssertCalled(t, "Publish", leaveGroupEventMatcher(groupID, userID))

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Leave Group group not found", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(nil, someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewLeaveGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &LeaveGroup{
			GroupID: groupID,
			UserID:  userID,
		}

		err := handler.LeaveGroup(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Leave Group player not in group", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createLeaveGroup(groupID, userID), nil)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewLeaveGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &LeaveGroup{
			GroupID: groupID,
			UserID:  "notInGroup",
		}

		err := handler.LeaveGroup(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Leave Group save error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createLeaveGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewLeaveGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &LeaveGroup{
			GroupID: groupID,
			UserID:  userID,
		}

		err := handler.LeaveGroup(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", leaveGroupMatcher())

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("Leave Group publish event error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createLeaveGroup(groupID, userID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(someErr)

		handler := NewLeaveGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &LeaveGroup{
			GroupID: groupID,
			UserID:  userID,
		}

		err := handler.LeaveGroup(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", leaveGroupMatcher())
		mockEventRepo.AssertCalled(t, "Publish", leaveGroupEventMatcher(groupID, userID))

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})
}

func leaveGroupMatcher() interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.Players()) == 2 && group.Players()[0].Status() == domain.Leaved
	})
}

func leaveGroupEventMatcher(groupID, userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.UserLeavesGroup)
		return ok && event.GroupID == groupID && event.UserID == userID
	})
}

func createLeaveGroup(groupID, userID string) *domain.Group {
	name, _ := domain.NewName("Group Name")

	return domain.NewGroup(
		groupID,
		[]*domain.Player{domain.NewPlayer(userID, domain.Active, domain.Member), domain.NewPlayer("master", domain.Active, domain.Master)},
		name,
		make([]string, 0),
		domain.Admin,
	)
}
