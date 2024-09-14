package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type GetGroup struct {
	GroupID string
}

type GetGroupHandler struct {
	domain.GroupRepository
	domain.UserRepository
}

func NewGetGroupHandler(
	groups domain.GroupRepository,
	users domain.UserRepository,
) GetGroupHandler {
	return GetGroupHandler{groups, users}
}

func (h GetGroupHandler) GetGroup(cmd *GetGroup) (*domain.GroupDetails, error) {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return nil, fmt.Errorf("getting group %s: %w", cmd.GroupID, err)
	}

	users, err := h.UserRepository.GetUserAll(group.UserIDs())
	if err != nil {
		return nil, fmt.Errorf("getting users %v: %w", group.UserIDs, err)
	}

	groupDetails := domain.NewGroupDetails(group, users)

	return groupDetails, nil
}
