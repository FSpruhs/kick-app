package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GetUserAll struct {
	Filter *Filter
}

type Filter struct {
	ExceptGroupID string
}

type GetUserAllHandler struct {
	domain.UserRepository
}

func NewGetUserAllHandler(users domain.UserRepository) GetUserAllHandler {
	return GetUserAllHandler{users}
}

func (h *GetUserAllHandler) GetUserAll(cmd *GetUserAll) ([]*domain.User, error) {

	users, err := h.UserRepository.FindAll(cmd.Filter)
	if err != nil {
		return nil, fmt.Errorf("get all users: %w", err)
	}

	return users, nil
}
