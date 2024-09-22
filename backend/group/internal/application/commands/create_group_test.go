package commands

import (
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

	expectedGroup := createExpectedGroup(name, userID)

	mockGroupRepo := setupMockGroupRepo(expectedGroup)
	mockEventRepo := setupMockEventRepo()

	handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

	cmd := &CreateGroup{
		UserID: userID,
		Name:   name.Value(),
	}

	t.Run("Create Group Success", func(t *testing.T) {
		group, err := handler.CreateGroup(cmd)

		assert.NoError(t, err)
		assert.NotNil(t, group)

		mockGroupRepo.AssertCalled(t, "Create", groupMatcher(name, userID))
		mockEventRepo.AssertCalled(t, "Publish", eventMatcher(userID))

		mockGroupRepo.AssertExpectations(t)
		mockEventRepo.AssertExpectations(t)
	})
}

func createExpectedGroup(name *domain.Name, userID string) *domain.Group {
	return &domain.Group{
		Aggregate:      ddd.NewAggregate("123", "Group"),
		Name:           name,
		Players:        []*domain.Player{domain.NewPlayer(userID, domain.Master, domain.Active)},
		InvitedUserIDs: make([]string, 0),
		InviteLevel:    domain.Admin,
	}
}

func setupMockGroupRepo(expectedGroup *domain.Group) *MockGroupRepository {
	mockGroupRepo := new(MockGroupRepository)
	mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(expectedGroup, nil)
	return mockGroupRepo
}

func setupMockEventRepo() *ddd.MockEventPublisher {
	mockEventRepo := new(ddd.MockEventPublisher)
	mockEventRepo.On("Publish", mock.AnythingOfType("[]ddd.AggregateEvent")).Return(nil)
	return mockEventRepo
}

func groupMatcher(name *domain.Name, userID string) interface{} {
	return mock.MatchedBy(func(g *domain.Group) bool {
		return g.InviteLevel == domain.Admin &&
			g.Name.Value() == name.Value() &&
			len(g.Players) == 1 &&
			g.Players[0].UserID() == userID &&
			g.Players[0].Status() == domain.Active &&
			g.Players[0].Role() == domain.Master &&
			len(g.InvitedUserIDs) == 0
	})
}

func eventMatcher(userID string) interface{} {
	return mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event, ok := events[0].Payload().(grouppb.GroupCreated)
		return ok && event.UserIDs[0] == userID
	})
}
