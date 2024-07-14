package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type PlayerDocument struct {
	ID      string `bson:"_id,omitempty"`
	GroupId string `json:"groupId,omitempty"`
	UserId  string `json:"userId,omitempty"`
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
		ID:      newPlayer.ID,
		GroupId: newPlayer.GroupID,
		UserId:  newPlayer.UserID,
	}
	_, err := p.collection.InsertOne(ctx, playerDoc)

	return newPlayer, err
}
