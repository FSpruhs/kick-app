package queries

import (
	"errors"
	"fmt"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

var ErrUserNotFound = fmt.Errorf("user not found")

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
		return nil, fmt.Errorf("getting users %s: %w", group.UserIDs(), err)
	}

	var notFoundUsers []string
	for _, player := range group.Players {
		user, err := findUser(users, player.UserID())
		if errors.Is(ErrUserNotFound, err) {
			notFoundUsers = append(notFoundUsers, player.UserID())
			continue
		}

		user.SetRole(player.Role().String())
		user.SetStatus(player.Status().String())

	}

	if len(notFoundUsers) > 0 {
		for _, user := range notFoundUsers {
			group.UserForPlayerNotFound(user)
		}
		if err := h.GroupRepository.Save(group); err != nil {
			return nil, fmt.Errorf("saving group %s: %w", group.ID(), err)
		}
	}

	groupDetails := domain.NewGroupDetails(group, users)

	return groupDetails, nil
}

func findUser(users []*domain.User, userID string) (*domain.User, error) {
	for _, user := range users {
		if user.ID() == userID {
			return user, nil
		}
	}
	return nil, ErrUserNotFound
}
