package queries

import "github.com/FSpruhs/kick-app/backend/group/internal/domain"

type IsPlayerActive struct {
	GroupID string
	UserID  string
}

type IsPlayerActiveHandler struct {
	domain.GroupRepository
}

func NewIsPlayerActiveHandler(groups domain.GroupRepository) IsPlayerActiveHandler {
	return IsPlayerActiveHandler{groups}
}

func (h IsPlayerActiveHandler) IsPlayerActive(cmd *IsPlayerActive) bool {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return false
	}

	return group.IsActivePlayer(cmd.UserID)
}
