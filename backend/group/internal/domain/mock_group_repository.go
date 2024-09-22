package domain

import (
	"github.com/stretchr/testify/mock"
)

type MockGroupRepository struct {
	mock.Mock
}

var _ GroupRepository = (*MockGroupRepository)(nil)

func (m *MockGroupRepository) FindByID(id string) (*Group, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}

	return args.Get(0).(*Group), args.Error(1)
}

func (m *MockGroupRepository) Save(group *Group) error {
	args := m.Called(group)
	return args.Error(0)
}

func (m *MockGroupRepository) Create(newGroup *Group) (*Group, error) {
	args := m.Called(newGroup)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}

	return args.Get(0).(*Group), args.Error(1)
}

func (m *MockGroupRepository) FindAllByUserID(userID string) ([]*Group, error) {
	//TODO implement me
	panic("implement me")
}
