package commands

import (
	"github.com/stretchr/testify/mock"
	"testing"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
	"github.com/stretchr/testify/assert"
)

func TestCreateUserHandler_CreateUser(t *testing.T) {
	fullName, _ := domain.NewFullName("John", "Doe")
	email, _ := domain.NewEmail("john.doe@example.com")
	password, _ := domain.NewPassword("Abcd123")

	mockRepo := new(MockUserRepository)
	mockRepo.On("CountByEmail", email).Return(0, nil)
	mockRepo.On("Create", mock.AnythingOfType("*domain.User")).Return(&domain.User{Id: "123"}, nil)

	handler := NewCreateUserHandler(mockRepo)
	cmd := &CreateUser{
		FullName: fullName,
		Nickname: "johndoe",
		Email:    email,
		Password: password,
	}

	createdUser, err := handler.CreateUser(cmd)

	assert.NoError(t, err)
	assert.NotNil(t, createdUser)
	assert.Equal(t, "123", createdUser.Id)

	mockRepo.AssertExpectations(t)
}

func TestCreateUserHandler_CreateUser_EmailAlreadyExists(t *testing.T) {
	fullName, _ := domain.NewFullName("John", "Doe")
	email, _ := domain.NewEmail("john.doe@example.com")
	password, _ := domain.NewPassword("Abcd123")

	mockRepo := new(MockUserRepository)
	mockRepo.On("CountByEmail", email).Return(1, nil)

	handler := NewCreateUserHandler(mockRepo)
	cmd := &CreateUser{
		FullName: fullName,
		Nickname: "johndoe",
		Email:    email,
		Password: password,
	}

	createdUser, err := handler.CreateUser(cmd)

	assert.NotNil(t, err)
	assert.Nil(t, createdUser)

	mockRepo.AssertExpectations(t)
}
