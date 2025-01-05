package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

type GetActivePlayersByGroup struct {
	GroupID string
}

type GetActivePlayersByGroupHandler struct {
	domain.GroupRepository
}

func NewGetActivePlayersByGroupHandler(groups domain.GroupRepository) GetActivePlayersByGroupHandler {
	return GetActivePlayersByGroupHandler{groups}
}

func (h GetActivePlayersByGroupHandler) GetActivePlayersByGroup(cmd *GetActivePlayersByGroup) ([]string, error) {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return nil, fmt.Errorf("getting group by id %s: %w", cmd.GroupID, err)
	}

	activePlayers := group.ActivePlayers()

	return activePlayers, nil
}
