package mongodb

import (
	"context"
	"fmt"
	"time"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

const timeout = 10 * time.Second

type GroupDocument struct {
	ID             string   `bson:"_id,omitempty"`
	Name           string   `json:"name,omitempty"`
	UserIDs        []string `json:"userIds,omitempty"`
	InvitedUserIDs []string `json:"invitedUserIds,omitempty"`
	InviteLevel    int      `json:"inviteLevel,omitempty"`
}

type GroupRepository struct {
	collection *mongo.Collection
}

var _ domain.GroupRepository = (*GroupRepository)(nil)

func NewGroupRepository(database *mongo.Database, collectionName string) GroupRepository {
	collection := database.Collection(collectionName)

	return GroupRepository{collection: collection}
}

func (g GroupRepository) FindByID(id string) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var groupDoc GroupDocument
	if err := g.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&groupDoc); err != nil {
		return nil, err
	}

	group, err := toDomain(&groupDoc)
	if err != nil {
		return nil, err
	}

	return group, nil
}

func (g GroupRepository) Save(group *domain.Group) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	groupDoc := toDocument(group)

	_, err := g.collection.ReplaceOne(ctx, bson.M{"_id": group.ID()}, groupDoc)

	return fmt.Errorf("while saving group err: %w", err)
}

func (g GroupRepository) Create(newGroup *domain.Group) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	groupDoc := toDocument(newGroup)

	_, err := g.collection.InsertOne(ctx, groupDoc)
	if err != nil {
		return nil, fmt.Errorf("while creating group err: %w", err)
	}

	return newGroup, fmt.Errorf("while creating group err: %w", err)
}

func toDocument(group *domain.Group) *GroupDocument {
	return &GroupDocument{
		ID:             group.ID(),
		Name:           group.Name.Value(),
		UserIDs:        group.UserIDs,
		InvitedUserIDs: group.InvitedUserIDs,
		InviteLevel:    group.InviteLevel,
	}
}

func toDomain(groupDoc *GroupDocument) (*domain.Group, error) {
	name, err := domain.NewName(groupDoc.Name)
	if err != nil {
		return nil, fmt.Errorf("while creating group: %w", err)
	}

	group := domain.NewGroup(groupDoc.ID)
	group.Name = name
	group.UserIDs = groupDoc.UserIDs
	group.InvitedUserIDs = groupDoc.InvitedUserIDs
	group.InviteLevel = groupDoc.InviteLevel

	return group, nil
}
