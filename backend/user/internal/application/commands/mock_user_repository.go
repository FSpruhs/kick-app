package commands

import (
	"github.com/stretchr/testify/mock"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type MockUserRepository struct {
	mock.Mock
}

var _ domain.UserRepository = (*MockUserRepository)(nil)

func (m *MockUserRepository) Save(user *domain.User) error {
	//TODO implement me
	panic("implement me")
}

func (m *MockUserRepository) FindByID(id string) (*domain.User, error) {
	//TODO implement me
	panic("implement me")
}

func (m *MockUserRepository) FindByIDs(ids []string) ([]*domain.User, error) {
	//TODO implement me
	panic("implement me")
}

func (m *MockUserRepository) FindAll(filter *domain.Filter) ([]*domain.User, error) {
	//TODO implement me
	panic("implement me")
}

func (m *MockUserRepository) FindByEmail(email *domain.Email) (*domain.User, error) {
	args := m.Called(email)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserRepository) Create(user *domain.User) (*domain.User, error) {
	args := m.Called(user)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserRepository) CountByEmail(email *domain.Email) (int, error) {
	args := m.Called(email)
	return args.Int(0), args.Error(1)
}
