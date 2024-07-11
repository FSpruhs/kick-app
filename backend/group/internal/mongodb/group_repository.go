package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type GroupDocument struct {
	Id             primitive.ObjectID `bson:"_id,omitempty"`
	Name           string             `json:"name,omitempty"`
	UserIds        []string           `json:"userIds,omitempty"`
	InvitedUserIds []string           `json:"invitedUserIds,omitempty"`
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

	objectId, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}

	var groupDoc GroupDocument
	err = g.collection.FindOne(ctx, GroupDocument{Id: objectId}).Decode(&groupDoc)
	if err != nil {
		return nil, err
	}

	group := domain.NewGroup(groupDoc.Id.Hex())
	group.Name = groupDoc.Name
	group.Users = groupDoc.UserIds
	group.InvitedUserIds = groupDoc.InvitedUserIds

	return group, nil
}

func (g GroupRepository) Save(group *domain.Group) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	objectId, err := primitive.ObjectIDFromHex(group.ID())
	if err != nil {
		return err
	}

	groupDoc := GroupDocument{
		Id:             objectId,
		Name:           group.Name,
		UserIds:        group.Users,
		InvitedUserIds: group.InvitedUserIds,
	}
	_, err = g.collection.ReplaceOne(ctx, GroupDocument{Id: objectId}, groupDoc)
	return err
}
