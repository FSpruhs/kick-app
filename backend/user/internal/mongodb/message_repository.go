package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/mongo"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

const timeout = 10 * time.Second

type MessageDocument struct {
	ID         string             `bson:"_id,omitempty"`
	UserID     string             `json:"userId,omitempty"`
	Content    string             `json:"content,omitempty"`
	Type       domain.MessageType `json:"type,omitempty"`
	OccurredAt time.Time          `json:"occurredAt,omitempty"`
	Read       bool               `json:"read,omitempty"`
}

type MessageRepository struct {
	collection *mongo.Collection
}

var _ domain.MessageRepository = (*MessageRepository)(nil)

func NewMessageRepository(db *mongo.Database, collectionName string) *MessageRepository {
	return &MessageRepository{collection: db.Collection(collectionName)}
}

func (m *MessageRepository) Create(message domain.Message) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	messageDoc := MessageDocument{
		ID:         message.ID,
		UserID:     message.UserID,
		Content:    message.Content,
		Type:       message.Type,
		OccurredAt: message.OccurredAt,
		Read:       message.Read,
	}

	_, err := m.collection.InsertOne(ctx, messageDoc)
	if err != nil {
		return fmt.Errorf("could not insert message: %w", err)
	}

	return nil
}
