package commands

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type InviteUser struct {
	GroupID string
	UserID  string
	PayerID string
}

type InviteUserHandler struct {
	domain.GroupRepository
	ddd.EventPublisher[ddd.AggregateEvent]
	domain.PlayerRepository
}

func NewInviteUserHandler(
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
	players domain.PlayerRepository,
) InviteUserHandler {
	return InviteUserHandler{groups, eventPublisher, players}
}

func (h InviteUserHandler) InviteUser(cmd *InviteUser) error {
	group, err := h.GroupRepository.FindById(cmd.GroupID)
	if err != nil {
		return domain.ErrGroupNotFound
	}

	if err := h.PlayerRepository.ConfirmPlayer(cmd.PayerID); err != nil {
		return err
	}
	group.InviteUser(cmd.UserID)

	if err := h.GroupRepository.Save(group); err != nil {
		return err
	}

	if err := h.Publish(group.Events()...); err != nil {
		return err
	}

	return nil
}
