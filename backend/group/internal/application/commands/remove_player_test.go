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

func TestRemovePlayerHandler_RemovePlayer(t *testing.T) {
	groupID := "123"
	removeUserID := "456"
	removingUserID := "789"

	someErr := errors.New("some error")

	t.Run("remove player success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createRemoveGroup(groupID, removeUserID, removingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewRemovePlayerHandler(mockGroupRepo, mockEventRepo)

		cmd := &RemovePlayer{
			GroupID:        groupID,
			RemoveUserID:   removeUserID,
			RemovingUserID: removingUserID,
		}

		err := handler.RemovePlayer(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", removeGroupMatcher())
		mockEventRepo.AssertCalled(t, "Publish", removeGroupEventMatcher(groupID, removeUserID))

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("remove player group not found", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(nil, someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewRemovePlayerHandler(mockGroupRepo, mockEventRepo)

		cmd := &RemovePlayer{
			GroupID:        groupID,
			RemoveUserID:   removeUserID,
			RemovingUserID: removingUserID,
		}

		err := handler.RemovePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("remove player not in group", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createRemoveGroup(groupID, removeUserID, removingUserID), nil)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewRemovePlayerHandler(mockGroupRepo, mockEventRepo)

		cmd := &RemovePlayer{
			GroupID:        groupID,
			RemoveUserID:   "not_in_group",
			RemovingUserID: removingUserID,
		}

		err := handler.RemovePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("remove player save failed", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createRemoveGroup(groupID, removeUserID, removingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(someErr)

		mockEventRepo := new(ddd.MockEventPublisher)

		handler := NewRemovePlayerHandler(mockGroupRepo, mockEventRepo)

		cmd := &RemovePlayer{
			GroupID:        groupID,
			RemoveUserID:   removeUserID,
			RemovingUserID: removingUserID,
		}

		err := handler.RemovePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", removeGroupMatcher())

		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("remove player publish failed", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createRemoveGroup(groupID, removeUserID, removingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(someErr)

		handler := NewRemovePlayerHandler(mockGroupRepo, mockEventRepo)

		cmd := &RemovePlayer{
			GroupID:        groupID,
			RemoveUserID:   removeUserID,
			RemovingUserID: removingUserID,
		}

		err := handler.RemovePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", removeGroupMatcher())
		mockEventRepo.AssertCalled(t, "Publish", removeGroupEventMatcher(groupID, removeUserID))

		mockEventRepo.AssertExpectations(t)
		mockGroupRepo.AssertExpectations(t)
	})
}

func removeGroupEventMatcher(groupID, userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.PlayerRemovedFromGroup)
		return ok && event.GroupID == groupID && event.UserID == userID
	})
}

func createRemoveGroup(groupID, removeUserID, removingUserID string) *domain.Group {
	name, _ := domain.NewName("Group Name")

	return &domain.Group{
		Aggregate:      ddd.NewAggregate(groupID, "Group"),
		Name:           name,
		Players:        []*domain.Player{domain.NewPlayer(removeUserID, domain.Active, domain.Member), domain.NewPlayer(removingUserID, domain.Active, domain.Master)},
		InvitedUserIDs: make([]string, 0),
		InviteLevel:    domain.Admin,
	}
}

func removeGroupMatcher() interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.Players) == 2 && group.Players[0].Status() == domain.Removed
	})
}
