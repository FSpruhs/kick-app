package commands

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/stretchr/testify/mock"
)

type MockGroupRepository struct {
	mock.Mock
}

var _ domain.GroupRepository = (*MockGroupRepository)(nil)

func (m MockGroupRepository) FindByID(id string) (*domain.Group, error) {
	//TODO implement me
	panic("implement me")
}

func (m MockGroupRepository) Save(group *domain.Group) error {
	//TODO implement me
	panic("implement me")
}

func (m MockGroupRepository) Create(newGroup *domain.Group) (*domain.Group, error) {
	args := m.Called(newGroup)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}

	return args.Get(0).(*domain.Group), args.Error(1)
}

func (m MockGroupRepository) FindAllByUserID(userID string) ([]*domain.Group, error) {
	//TODO implement me
	panic("implement me")
}
