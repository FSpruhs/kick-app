package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GetUser struct {
	UserID string
}

type GetUserHandler struct {
	domain.UserRepository
}

func NewGetUserHandler(users domain.UserRepository) GetUserHandler {
	return GetUserHandler{users}
}

func (h GetUserHandler) GetUser(cmd *GetUser) (*domain.User, error) {
	user, err := h.UserRepository.FindByID(cmd.UserID)
	if err != nil {
		return nil, fmt.Errorf("getting user %s: %w", cmd.UserID, err)
	}

	return user, nil
}
