package commands

import (
	"errors"
	"testing"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestCreateGroupHandler_CreateGroup(t *testing.T) {
	name, _ := domain.NewName("Group Name")
	userID := "123"

	someErr := errors.New("some error")

	expectedGroup := createExpectedGroup(name, userID)

	t.Run("Create Group Success", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(expectedGroup, nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &CreateGroup{
			UserID: userID,
			Name:   name.Value(),
		}

		group, err := handler.CreateGroup(cmd)

		assert.NoError(t, err)
		assert.NotNil(t, group)

		mockGroupRepo.AssertCalled(t, "Create", groupMatcher(name, userID))
		mockEventRepo.AssertCalled(t, "Publish", createEventMatcher(userID))

		mockGroupRepo.AssertExpectations(t)
		mockEventRepo.AssertExpectations(t)
	})

	t.Run("Create Group invalid group creating", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(expectedGroup, nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &CreateGroup{
			UserID: userID,
			Name:   "",
		}

		group, err := handler.CreateGroup(cmd)

		assert.Error(t, err)
		assert.Nil(t, group)
	})

	t.Run("Create group with repo error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(nil, someErr)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)

		handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &CreateGroup{
			UserID: userID,
			Name:   name.Value(),
		}

		group, err := handler.CreateGroup(cmd)

		assert.Error(t, err)
		assert.Nil(t, group)
	})

	t.Run("Create group with publisher error", func(t *testing.T) {
		mockGroupRepo := new(domain.MockGroupRepository)
		mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(expectedGroup, nil)

		mockEventRepo := new(ddd.MockEventPublisher)
		mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(someErr)

		handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

		cmd := &CreateGroup{
			UserID: userID,
			Name:   name.Value(),
		}

		group, err := handler.CreateGroup(cmd)

		assert.Error(t, err)
		assert.Nil(t, group)
	})
}

func createExpectedGroup(name *domain.Name, userID string) *domain.Group {
	return domain.NewGroup(
		"123",
		[]*domain.Player{domain.NewPlayer(userID, domain.Master, domain.Active)},
		name,
		make([]string, 0),
		domain.Admin,
	)
}

func groupMatcher(name *domain.Name, userID string) interface{} {
	return mock.MatchedBy(func(g *domain.Group) bool {
		return g.InviteLevel() == domain.Admin &&
			g.Name().Value() == name.Value() &&
			len(g.Players()) == 1 &&
			g.Players()[0].UserID() == userID &&
			g.Players()[0].Status() == domain.Active &&
			g.Players()[0].Role() == domain.Master &&
			len(g.InvitedUserIDs()) == 0
	})
}

func createEventMatcher(userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.GroupCreated)
		return ok && event.UserIDs[0] == userID
	})
}
