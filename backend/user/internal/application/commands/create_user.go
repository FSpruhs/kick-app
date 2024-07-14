package commands

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type CreateUser struct {
	FullName *domain.FullName
	Nickname string
	Email    *domain.Email
	Password *domain.Password
}

type CreateUserHandler struct {
	domain.UserRepository
}

func NewCreateUserHandler(users domain.UserRepository) CreateUserHandler {
	return CreateUserHandler{users}
}

func (h CreateUserHandler) CreateUser(cmd *CreateUser) (*domain.User, error) {
	if emailCount, err := h.UserRepository.CountByEmail(cmd.Email); err != nil {
		return nil, err
	} else if emailCount > 0 {
		return nil, domain.ErrEmailAlreadyExists
	}

	newUser := domain.NewUser(cmd.FullName, cmd.Nickname, cmd.Password, cmd.Email)
	result, err := h.UserRepository.Create(newUser)
	if err != nil {
		return nil, fmt.Errorf("while creating db err: %w", err)
	}

	return result, nil
}
