package commands

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type MessageRead struct {
	UserID    string
	MessageID string
	Read      bool
}

type MessageReadHandler struct {
	domain.MessageRepository
}

func NewMessageReadHandler(messages domain.MessageRepository) MessageReadHandler {
	return MessageReadHandler{messages}
}

func (h MessageReadHandler) MessageRead(cmd *MessageRead) error {
	message, err := h.MessageRepository.FindByID(cmd.MessageID)
	if err != nil {
		return fmt.Errorf("finding message with id %s: %w", cmd.MessageID, err)
	}

	if message.UserID != cmd.UserID {
		return errors.New("message does not belong to user")
	}

	message.Read = cmd.Read

	if err := h.MessageRepository.Save(message); err != nil {
		return fmt.Errorf("saving message with id %s: %w", cmd.MessageID, err)
	}

	return nil
}
