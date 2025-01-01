package queries

import "github.com/FSpruhs/kick-app/backend/group/internal/domain"

type HasPlayerAdminRole struct {
	UserID  string
	GroupID string
}

type HasPlayerAdminRoleHandler struct {
	groups domain.GroupRepository
}

func NewHasPlayerAdminRoleHandler(groups domain.GroupRepository) HasPlayerAdminRoleHandler {
	return HasPlayerAdminRoleHandler{groups: groups}
}

func (h HasPlayerAdminRoleHandler) HasPlayerAdminRole(cmd *HasPlayerAdminRole) bool {
	group, err := h.groups.FindByID(cmd.GroupID)
	if err != nil {
		return false
	}

	return group.HasPlayerAdminRole(cmd.UserID)
}
