package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type GroupDocument struct {
	Id             string   `bson:"_id,omitempty"`
	Name           string   `json:"name,omitempty"`
	UserIds        []string `json:"userIds,omitempty"`
	InvitedUserIds []string `json:"invitedUserIds,omitempty"`
}

type GroupRepository struct {
	collection *mongo.Collection
}

var _ domain.GroupRepository = (*GroupRepository)(nil)

func NewGroupRepository(database *mongo.Database, collectionName string) GroupRepository {
	collection := database.Collection(collectionName)
	return GroupRepository{collection: collection}
}

func (g GroupRepository) FindById(id string) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
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
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	groupDoc := toDocument(group)

	_, err := g.collection.ReplaceOne(ctx, bson.M{"_id": group.ID()}, groupDoc)
	return err
}

func (g GroupRepository) Create(newGroup *domain.Group) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	groupDoc := toDocument(newGroup)

	_, err := g.collection.InsertOne(ctx, groupDoc)
	if err != nil {
		return nil, err
	}

	return newGroup, err
}

func toDocument(group *domain.Group) *GroupDocument {
	return &GroupDocument{
		Id:             group.ID(),
		Name:           group.Name.Value(),
		UserIds:        group.UserIds,
		InvitedUserIds: group.InvitedUserIds,
	}
}

func toDomain(groupDoc *GroupDocument) (*domain.Group, error) {
	name, err := domain.NewName(groupDoc.Name)
	if err != nil {
		return nil, err
	}

	group := domain.NewGroup(groupDoc.Id)
	group.Name = name
	group.UserIds = groupDoc.UserIds
	group.InvitedUserIds = groupDoc.InvitedUserIds

	return group, nil
}
