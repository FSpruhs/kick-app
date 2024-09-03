package mongodb

import (
	"context"
	"fmt"
	"time"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

const timeout = 10 * time.Second

var _ domain.PlayerRepository = (*PlayerRepository)(nil)

type PlayerDocument struct {
	ID      string `bson:"_id,omitempty"`
	GroupID string `json:"groupId,omitempty"`
	UserID  string `json:"userId,omitempty"`
	Role    int    `json:"role,omitempty"`
}

type PlayerRepository struct {
	collection *mongo.Collection
}

func NewPlayerRepository(database *mongo.Database, collectionName string) PlayerRepository {
	return PlayerRepository{collection: database.Collection(collectionName)}
}

func (p PlayerRepository) FindByID(id string) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var playerDoc PlayerDocument
	if err := p.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&playerDoc); err != nil {
		return nil, fmt.Errorf("while finding player err: %w", err)
	}

	player := domain.Player{
		Aggregate: ddd.NewAggregate(playerDoc.ID, domain.PlayerAggregate),
		GroupID:   playerDoc.GroupID,
		UserID:    playerDoc.UserID,
		Role:      domain.PlayerRole(playerDoc.Role),
	}

	return &player, nil
}

func (p PlayerRepository) Create(newPlayer *domain.Player) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	playerDoc := toDocument(newPlayer)
	_, err := p.collection.InsertOne(ctx, playerDoc)

	return newPlayer, fmt.Errorf("while creating player err: %w", err)
}

func (p PlayerRepository) Save(player *domain.Player) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	playerDoc := toDocument(player)

	_, err := p.collection.ReplaceOne(ctx, bson.M{"_id": player.ID()}, playerDoc)

	return fmt.Errorf("while saving group err: %w", err)
}

func toDocument(player *domain.Player) PlayerDocument {
	return PlayerDocument{
		ID:      player.ID(),
		GroupID: player.GroupID,
		UserID:  player.UserID,
		Role:    int(player.Role),
	}
}
