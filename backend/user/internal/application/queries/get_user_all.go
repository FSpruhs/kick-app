package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GetUserAll struct {
	UserIDs []string
}

type GetUserAllHandler struct {
	domain.UserRepository
}

func NewGetUserAllHandler(users domain.UserRepository) GetUserAllHandler {
	return GetUserAllHandler{users}
}

func (h GetUserAllHandler) GetUserAll(cmd *GetUserAll) ([]*domain.User, error) {
	users, err := h.UserRepository.FindByIDs(cmd.UserIDs)
	if err != nil {
		return nil, fmt.Errorf("getting users: %w", err)
	}

	return users, nil
}
