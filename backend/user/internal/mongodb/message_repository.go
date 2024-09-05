package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
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

func (m *MessageRepository) Create(message *domain.Message) error {
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

func (m *MessageRepository) FindByID(id string) (*domain.Message, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var messageDoc MessageDocument
	if err := m.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&messageDoc); err != nil {
		return nil, fmt.Errorf("finding message by id: %w", err)
	}

	message := domain.Message{
		ID:         messageDoc.ID,
		UserID:     messageDoc.UserID,
		Content:    messageDoc.Content,
		Type:       messageDoc.Type,
		OccurredAt: messageDoc.OccurredAt,
		Read:       messageDoc.Read,
	}

	return &message, nil
}

func (m *MessageRepository) Save(message *domain.Message) error {
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

	_, err := m.collection.ReplaceOne(ctx, bson.M{"_id": message.ID}, messageDoc)
	if err != nil {
		return fmt.Errorf("saving message: %w", err)
	}

	return nil
}
