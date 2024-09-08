package queries

import (
	"fmt"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type GetGroups struct {
	UserID string
}

type GetGroupsHandler struct {
	domain.GroupRepository
}

func NewGetGroupsHandler(groups domain.GroupRepository) GetGroupsHandler {
	return GetGroupsHandler{groups}
}

func (h GetGroupsHandler) GetGroups(cmd *GetGroups) ([]*domain.Group, error) {
	groups, err := h.GroupRepository.FindAllByUserID(cmd.UserID)
	if err != nil {
		return nil, fmt.Errorf("getting groups for user %s: %w", cmd.UserID, err)
	}

	return groups, nil
}
