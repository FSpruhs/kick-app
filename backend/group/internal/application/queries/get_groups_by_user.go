package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type GetGroupsByUser struct {
	UserID string
}

type GetGroupsByUserHandler struct {
	domain.GroupRepository
}

func NewGetGroupsByUserHandler(groups domain.GroupRepository) GetGroupsByUserHandler {
	return GetGroupsByUserHandler{groups}
}

func (h GetGroupsByUserHandler) GetGroups(cmd *GetGroupsByUser) ([]*domain.Group, error) {
	groups, err := h.GroupRepository.FindAllByUserID(cmd.UserID)
	if err != nil {
		return nil, fmt.Errorf("getting groups for user %s: %w", cmd.UserID, err)
	}

	filteredGroups := filterGroups(cmd.UserID, groups)

	return filteredGroups, nil
}

func filterGroups(userID string, groups []*domain.Group) []*domain.Group {
	var filteredGroups []*domain.Group

	for _, group := range groups {
		if group.IsUserParticipateInTheGroup(userID) {
			filteredGroups = append(filteredGroups, group)
		}
	}
	return filteredGroups
}
