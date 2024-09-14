package queries

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GetUserMessages struct {
	UserID string
}

type GetUserMessagesHandler struct {
	domain.MessageRepository
}

func NewGetUserMessagesHandler(messages domain.MessageRepository) GetUserMessagesHandler {
	return GetUserMessagesHandler{messages}
}

func (h GetUserMessagesHandler) GetUserMessages(cmd *GetUserMessages) ([]*domain.Message, error) {
	messages, err := h.MessageRepository.FindByUserID(cmd.UserID)
	if err != nil {
		return nil, fmt.Errorf("getting messages for user %s: %w", cmd.UserID, err)
	}

	return messages, nil
}
