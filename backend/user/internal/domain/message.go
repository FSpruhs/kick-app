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
	MatchID    string
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
		MatchID:    "",
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
		MatchID:    "",
		Content:    fmt.Sprintf("You have been invited to %s!", groupName),
		Type:       GroupInvitation,
		OccurredAt: time.Now(),
		Read:       false,
	}
}

func CreateInviteUserToMatchMessage(userID, matchID, groupID string) *Message {
	return &Message{
		ID:         uuid.New().String(),
		UserID:     userID,
		GroupID:    groupID,
		MatchID:    matchID,
		Content:    fmt.Sprint("You have been invited to a match!"),
		Type:       MatchInvitation,
		OccurredAt: time.Now(),
		Read:       false,
	}
}

func (m *Message) MarkAsRead() {
	m.Read = true
}
