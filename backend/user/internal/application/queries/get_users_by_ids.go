package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GetUsersByIDs struct {
	UserIDs []string
}

type GetUsersByIDsHandler struct {
	domain.UserRepository
}

func NewGetUsersByIDsHandler(users domain.UserRepository) GetUsersByIDsHandler {
	return GetUsersByIDsHandler{users}
}

func (h GetUsersByIDsHandler) GetUsersByIDs(cmd *GetUsersByIDs) ([]*domain.User, error) {
	users, err := h.UserRepository.FindByIDs(cmd.UserIDs)
	if err != nil {
		return nil, fmt.Errorf("getting users: %w", err)
	}

	return users, nil
}
