package commands

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type InvitedUserResponse struct {
	GroupID string
	UserID  string
	Accept  bool
}

type InvitedUserResponseHandler struct {
	domain.GroupRepository
	ddd.EventPublisher[ddd.AggregateEvent]
}

func NewInvitedUserResponseHandler(
	groups domain.GroupRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) InvitedUserResponseHandler {
	return InvitedUserResponseHandler{groups, eventPublisher}
}

func (h InvitedUserResponseHandler) InvitedUserResponse(cmd *InvitedUserResponse) error {
	group, err := h.GroupRepository.FindByID(cmd.GroupID)
	if err != nil {
		return fmt.Errorf("handle invited user response: %w", err)
	}

	if err := group.HandleInvitedUserResponse(cmd.UserID, cmd.Accept); err != nil {
		return fmt.Errorf("handle invited user response: %w", err)
	}

	if err := h.GroupRepository.Save(group); err != nil {
		return fmt.Errorf("handle invited user response: %w", err)
	}

	if err := h.Publish(group.Events()...); err != nil {
		return fmt.Errorf("handle invited user response: %w", err)
	}

	return nil
}
