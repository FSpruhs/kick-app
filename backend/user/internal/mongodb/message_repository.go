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
	GroupID    string             `json:"groupId,omitempty"`
	MatchID    string             `json:"matchId,omitempty"`
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

	messageDoc := toDocument(message)

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

	message := toMessageDomain(&messageDoc)

	return message, nil
}

func (m *MessageRepository) Save(message *domain.Message) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	messageDoc := toDocument(message)

	_, err := m.collection.ReplaceOne(ctx, bson.M{"_id": message.ID}, messageDoc)
	if err != nil {
		return fmt.Errorf("saving message: %w", err)
	}

	return nil
}

func (m *MessageRepository) FindByUserID(userID string) ([]*domain.Message, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	cursor, err := m.collection.Find(ctx, bson.M{"userid": userID})
	if err != nil {
		return nil, fmt.Errorf("finding messages by user id: %w", err)
	}

	defer func() { _ = cursor.Close(ctx) }()

	var messageDocs []*MessageDocument

	if err := cursor.All(ctx, &messageDocs); err != nil {
		return nil, fmt.Errorf("iterating over messages: %w", err)
	}

	messages := make([]*domain.Message, len(messageDocs))
	for index, messageDoc := range messageDocs {
		messages[index] = toMessageDomain(messageDoc)
	}

	return messages, nil
}

func toDocument(message *domain.Message) *MessageDocument {
	return &MessageDocument{
		ID:         message.ID,
		UserID:     message.UserID,
		GroupID:    message.GroupID,
		MatchID:    message.MatchID,
		Content:    message.Content,
		Type:       message.Type,
		OccurredAt: message.OccurredAt,
		Read:       message.Read,
	}
}

func toMessageDomain(messageDoc *MessageDocument) *domain.Message {
	return &domain.Message{
		ID:         messageDoc.ID,
		UserID:     messageDoc.UserID,
		GroupID:    messageDoc.GroupID,
		MatchID:    messageDoc.MatchID,
		Content:    messageDoc.Content,
		Type:       messageDoc.Type,
		OccurredAt: messageDoc.OccurredAt,
		Read:       messageDoc.Read,
	}
}
