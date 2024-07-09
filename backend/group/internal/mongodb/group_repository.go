package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type GroupDocument struct {
	Id      primitive.ObjectID `bson:"_id,omitempty"`
	Name    string             `json:"name,omitempty"`
	UserIds []string           `json:"userIds,omitempty"`
}

type GroupRepository struct {
	collection *mongo.Collection
}

var _ domain.GroupRepository = (*GroupRepository)(nil)

func NewGroupRepository(database *mongo.Database, collectionName string) GroupRepository {
	collection := database.Collection(collectionName)
	return GroupRepository{collection: collection}
}

func (g GroupRepository) Create(newGroup *domain.Group) (*domain.Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	groupDoc := GroupDocument{
		Id:      primitive.NewObjectID(),
		Name:    newGroup.Name,
		UserIds: newGroup.Users,
	}
	result, err := g.collection.InsertOne(ctx, groupDoc)
	if id, ok := result.InsertedID.(primitive.ObjectID); ok {
		newGroup.SetId(id.Hex())
	}

	return newGroup, err
}
