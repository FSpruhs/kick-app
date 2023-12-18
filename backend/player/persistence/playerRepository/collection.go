package playerRepository

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/config"
	"github.com/FSpruhs/kick-app/backend/player/domain/player"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type PlayerCollection struct {
	collection *mongo.Collection
}

func New(client *mongo.Client) *PlayerCollection {
	return &PlayerCollection{collection: getCollection(client, "player")}
}

func getCollection(client *mongo.Client, collectionName string) *mongo.Collection {
	return client.Database(config.DatabaseName()).Collection(collectionName)
}

func (p *PlayerCollection) Create(newPlayer *player.Player) (*player.Player, error) {
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
