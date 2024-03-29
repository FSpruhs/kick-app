package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type PlayerDocument struct {
	FirstName string `json:"firstName,omitempty"`
	LastName  string `json:"lastName,omitempty"`
}

type PlayerRepository struct {
	collection *mongo.Collection
}

func NewPlayerRepository(database *mongo.Database, collectionName string) PlayerRepository {
	return PlayerRepository{collection: database.Collection(collectionName)}
}

func (p PlayerRepository) Create(newPlayer *domain.Player) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	playerDoc := PlayerDocument{
		FirstName: newPlayer.FirstName,
		LastName:  newPlayer.LastName,
	}
	result, err := p.collection.InsertOne(ctx, playerDoc)
	if id, ok := result.InsertedID.(primitive.ObjectID); ok {
		newPlayer.SetId(id.Hex())
	}

	return newPlayer, err
}
