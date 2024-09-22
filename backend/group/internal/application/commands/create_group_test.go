package commands

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"testing"
)

func TestCreateGroupHandler_CreateGroup(t *testing.T) {

	name, _ := domain.NewName("Group Name")

	expected := &domain.Group{
		Aggregate:      ddd.NewAggregate("123", "Group"),
		Name:           name,
		Players:        make([]*domain.Player, 0),
		InvitedUserIDs: make([]string, 0),
		InviteLevel:    domain.Admin,
	}

	mockGroupRepo := new(MockGroupRepository)
	mockGroupRepo.On("Create", mock.AnythingOfType("*domain.Group")).Return(expected, nil)

	mockEventRepo := new(ddd.MockEventPublisher)
	mockEventRepo.On("Publish", mock.AnythingOfType("[]*domain.AggregateEvent")).Return(nil)

	handler := NewCreateGroupHandler(mockGroupRepo, mockEventRepo)

	cmd := &CreateGroup{
		UserID: "123",
		Name:   "Group Name",
	}

	group, err := handler.CreateGroup(cmd)

	eventMatcher := mock.MatchedBy(func(events []ddd.AggregateEvent) bool {
		if len(events) != 1 {
			return false
		}
		event := events[0].Payload().(grouppb.GroupCreated)

		return event.UserIDs[0] == "123"
	})

	assert.NoError(t, err)
	assert.NotNil(t, group)

	mockEventRepo.AssertCalled(t, "Publish", eventMatcher)

	mockGroupRepo.AssertExpectations(t)
	mockEventRepo.AssertExpectations(t)

}
