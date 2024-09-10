package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
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

	return GroupRepository{collection}
}

func (g GroupRepository) FindByID(id string) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var groupDoc GroupDocument
	if err := g.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&groupDoc); err != nil {
		return nil, fmt.Errorf("finding group by id: %w", err)
	}

	group, err := toDomain(&groupDoc)
	if err != nil {
		return nil, fmt.Errorf("mapping group document to domain: %w", err)
	}

	return group, nil
}

func (g GroupRepository) Save(group *domain.Group) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	groupDoc := toDocument(group)

	_, err := g.collection.ReplaceOne(ctx, bson.M{"_id": group.ID()}, groupDoc)
	if err != nil {
		return fmt.Errorf("saving group %s: %w", group.ID(), err)
	}

	return nil
}

func (g GroupRepository) Create(newGroup *domain.Group) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	groupDoc := toDocument(newGroup)

	_, err := g.collection.InsertOne(ctx, groupDoc)
	if err != nil {
		return nil, fmt.Errorf("creating new group group: %w", err)
	}

	return newGroup, nil
}

func (g GroupRepository) FindAllByUserID(userID string) ([]*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	filter := bson.M{"userids": bson.M{"$in": []string{userID}}}

	cursor, err := g.collection.Find(ctx, filter)
	if err != nil {
		return nil, fmt.Errorf("while finding groups: %w", err)
	}

	defer cursor.Close(ctx)

	var groupDocs []*GroupDocument
	if err := cursor.All(ctx, &groupDocs); err != nil {
		return nil, fmt.Errorf("while finding groups: %w", err)
	}

	groups, err := toDomains(groupDocs)
	if err != nil {
		return nil, fmt.Errorf("while mapping group documents to domains: %w", err)
	}

	return groups, nil
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
		return nil, fmt.Errorf("while mapping group document do domain: %w", err)
	}

	group := domain.NewGroup(groupDoc.ID)
	group.Name = name
	group.UserIDs = groupDoc.UserIDs
	group.InvitedUserIDs = groupDoc.InvitedUserIDs
	group.InviteLevel = groupDoc.InviteLevel

	return group, nil
}

func toDomains(groupDocs []*GroupDocument) ([]*domain.Group, error) {
	groups := make([]*domain.Group, len(groupDocs))

	for index, groupDoc := range groupDocs {
		group, err := toDomain(groupDoc)
		if err != nil {
			return nil, fmt.Errorf("mapping group documents to domains: %w", err)
		}

		groups[index] = group
	}

	return groups, nil
}
