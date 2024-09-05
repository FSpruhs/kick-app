package commands

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

func TestLoginUserHandler_SuccessfulLogin(t *testing.T) {
	var fullName, _ = domain.NewFullName("John", "Doe")
	var password, _ = domain.NewPassword("Password123")
	var email, _ = domain.NewEmail("john.doe@example.com")
	mockUser := &domain.User{
		Id:       "123",
		FullName: fullName,
		Email:    email,
		Password: password,
	}
	mockRepo := new(MockUserRepository)
	mockRepo.On("FindByEmail", mock.AnythingOfType("*domain.Email")).Return(mockUser, nil)

	handler := NewLoginUserHandler(mockRepo)

	cmd := LoginUser{
		Email:    mockUser.Email,
		Password: mockUser.Password,
	}
	user, err := handler.LoginUser(cmd)

	assert.NoError(t, err, "Expected no error during successful login, but got %v", err)
	assert.NotNil(t, user, "Expected user object to be returned, but got nil")
	assert.Equal(t, mockUser.Id, user.Id, "Expected user ID '%s', but got '%s'", mockUser.Id, user.Id)

	mockRepo.AssertExpectations(t)
}

func TestLoginUserHandler_UserNotFound(t *testing.T) {
	mockRepo := new(MockUserRepository)
	mockRepo.On("FindByEmail", mock.AnythingOfType("*domain.Email")).Return(nil, domain.ErrEmailInvalid)

	handler := NewLoginUserHandler(mockRepo)

	var email, _ = domain.NewEmail("nonexisting@example.com")
	var password, _ = domain.NewPassword("Password123")
	cmd := LoginUser{
		Email:    email,
		Password: password,
	}
	user, err := handler.LoginUser(cmd)

	assert.ErrorIs(t, err, domain.ErrEmailInvalid, "Expected error due to user not found")
	assert.Nil(t, user, "Expected no user object to be returned when user is not found")

	mockRepo.AssertExpectations(t)
}

func TestLoginUserHandler_WrongPassword(t *testing.T) {
	var fullName, _ = domain.NewFullName("John", "Doe")
	var password, _ = domain.NewPassword("Password123")
	var email, _ = domain.NewEmail("john.doe@example.com")
	mockUser := &domain.User{
		Id:       "123",
		FullName: fullName,
		Email:    email,
		Password: password,
	}
	mockRepo := new(MockUserRepository)
	mockRepo.On("FindByEmail", mock.AnythingOfType("*domain.Email")).Return(mockUser, nil)

	handler := NewLoginUserHandler(mockRepo)

	var wrongPassword, _ = domain.NewPassword("Wrong123")
	cmd := LoginUser{
		Email:    mockUser.Email,
		Password: wrongPassword,
	}
	user, err := handler.LoginUser(cmd)

	assert.ErrorIs(t, err, domain.ErrWrongPassword, "Expected error due to wrong password")
	assert.Nil(t, user, "Expected no user object to be returned when password is wrong")

	mockRepo.AssertExpectations(t)
}
