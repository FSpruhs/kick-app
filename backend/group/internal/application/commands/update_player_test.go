package commands

import (
	"errors"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"testing"
)

func TestUpdatePlayerHandler_UpdatePlayer(t *testing.T) {
	groupID := "123"
	updatingUserID := "456"
	updatedUserID := "789"
	newRole := domain.Admin
	newStatus := domain.Inactive

	someErr := errors.New("some error")

	t.Run("update player success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createUpdateGroup(groupID, updatedUserID, updatingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(nil)

		handler := NewUpdatePlayerHandler(mockGroupRepo)

		cmd := &UpdatePlayer{
			GroupID:        groupID,
			UpdatingUserID: updatingUserID,
			UpdatedUserID:  updatedUserID,
			NewRole:        domain.Role(newRole),
			NewStatus:      domain.Status(newStatus),
		}

		err := handler.UpdatePlayer(cmd)

		assert.NoError(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", updateGroupMatcher(domain.Role(newRole), domain.Status(newStatus)))
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("update player group not found", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(nil, someErr)

		handler := NewUpdatePlayerHandler(mockGroupRepo)

		cmd := &UpdatePlayer{
			GroupID:        groupID,
			UpdatingUserID: updatingUserID,
			UpdatedUserID:  updatedUserID,
			NewRole:        domain.Role(newRole),
			NewStatus:      domain.Status(newStatus),
		}

		err := handler.UpdatePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("update player not found", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createUpdateGroup(groupID, updatedUserID, updatingUserID), nil)

		handler := NewUpdatePlayerHandler(mockGroupRepo)

		cmd := &UpdatePlayer{
			GroupID:        groupID,
			UpdatingUserID: updatingUserID,
			UpdatedUserID:  "notFound",
			NewRole:        domain.Role(newRole),
			NewStatus:      domain.Status(newStatus),
		}

		err := handler.UpdatePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertExpectations(t)
	})

	t.Run("update player save error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("FindByID", mock.AnythingOfType("string")).Return(createUpdateGroup(groupID, updatedUserID, updatingUserID), nil)
		mockGroupRepo.On("Save", mock.AnythingOfType("*domain.Group")).Return(someErr)

		handler := NewUpdatePlayerHandler(mockGroupRepo)

		cmd := &UpdatePlayer{
			GroupID:        groupID,
			UpdatingUserID: updatingUserID,
			UpdatedUserID:  updatedUserID,
			NewRole:        domain.Role(newRole),
			NewStatus:      domain.Status(newStatus),
		}

		err := handler.UpdatePlayer(cmd)

		assert.Error(t, err)

		mockGroupRepo.AssertCalled(t, "FindByID", groupID)
		mockGroupRepo.AssertCalled(t, "Save", updateGroupMatcher(domain.Role(newRole), domain.Status(newStatus)))
		mockGroupRepo.AssertExpectations(t)
	})

}

func createUpdateGroup(groupID, updatedUserID, updatingUserID string) *domain.Group {
	name, _ := domain.NewName("Group Name")

	return domain.NewGroup(
		groupID,
		[]*domain.Player{domain.NewPlayer(updatedUserID, domain.Active, domain.Member), domain.NewPlayer(updatingUserID, domain.Active, domain.Master)},
		name,
		make([]string, 0),
		domain.Admin,
	)
}

func updateGroupMatcher(newRole domain.Role, newStatus domain.Status) interface{} {
	return mock.MatchedBy(func(group *domain.Group) bool {
		return len(group.Players()) == 2 && group.Players()[0].Status() == newStatus && group.Players()[0].Role() == newRole
	})
}
