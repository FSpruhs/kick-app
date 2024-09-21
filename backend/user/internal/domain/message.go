package domain

import (
	"fmt"
	"time"

	"github.com/google/uuid"
)

type Message struct {
	ID         string
	UserID     string
	GroupID    string
	Content    string
	Type       MessageType
	OccurredAt time.Time
	Read       bool
}

func CreateRemovedFromGroupMessage(userID, groupID, groupName string) *Message {
	return &Message{
		ID:         uuid.New().String(),
		UserID:     userID,
		GroupID:    groupID,
		Content:    fmt.Sprintf("You have been removed from %s!", groupName),
		Type:       RemovedFromGroup,
		OccurredAt: time.Now(),
		Read:       false,
	}
}

func CreateGroupInvitationMessage(userID, groupID, groupName string) *Message {
	return &Message{
		ID:         uuid.New().String(),
		UserID:     userID,
		GroupID:    groupID,
		Content:    fmt.Sprintf("You have been invited to %s!", groupName),
		Type:       GroupInvitation,
		OccurredAt: time.Now(),
		Read:       false,
	}
}

func (m *Message) MarkAsRead() {
	m.Read = true
}
