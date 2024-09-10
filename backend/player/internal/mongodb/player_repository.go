package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
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

func (p PlayerRepository) FindByUserIDAndGroupID(userID, groupID string) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var playerDoc PlayerDocument
	if err := p.collection.FindOne(ctx, bson.M{"userId": userID, "groupId": groupID}).Decode(&playerDoc); err != nil {
		return nil, fmt.Errorf("finding player by user id and group id: %w", err)
	}

	player := toPlayer(&playerDoc)

	return player, nil
}

func (p PlayerRepository) FindByID(id string) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var playerDoc PlayerDocument
	if err := p.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&playerDoc); err != nil {
		return nil, fmt.Errorf("finding player by id: %w", err)
	}

	player := toPlayer(&playerDoc)

	return player, nil
}

func (p PlayerRepository) Create(newPlayer *domain.Player) (*domain.Player, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	playerDoc := toDocument(newPlayer)

	_, err := p.collection.InsertOne(ctx, playerDoc)
	if err != nil {
		return nil, fmt.Errorf("creating player in db: %w", err)
	}

	return newPlayer, nil
}

func (p PlayerRepository) Save(player *domain.Player) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	playerDoc := toDocument(player)

	_, err := p.collection.ReplaceOne(ctx, bson.M{"_id": player.ID()}, playerDoc)
	if err != nil {
		return fmt.Errorf("saving player in db: %w", err)
	}

	return nil
}

func (p PlayerRepository) SaveAll(players []*domain.Player) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	session, err := p.collection.Database().Client().StartSession()
	if err != nil {
		return fmt.Errorf("starting session: %w", err)
	}
	defer session.EndSession(ctx)

	if err := session.StartTransaction(); err != nil {
		return fmt.Errorf("starting transaction: %w", err)
	}

	if err := mongo.WithSession(ctx, session, func(sessionContext mongo.SessionContext) error {
		for _, player := range players {
			if err := p.savePlayerInTransaction(sessionContext, player); err != nil {
				if err := session.AbortTransaction(sessionContext); err != nil {
					return fmt.Errorf("while aborting transaction: %w", err)
				}
				return fmt.Errorf("while saving player %s: %w", player.ID(), err)
			}
		}

		return session.CommitTransaction(sessionContext)
	}); err != nil {
		return fmt.Errorf("transaction failed: %w", err)
	}

	return nil
}

func (p PlayerRepository) savePlayerInTransaction(sessionContext mongo.SessionContext, player *domain.Player) error {
	playerDoc := toDocument(player)
	_, err := p.collection.ReplaceOne(sessionContext, bson.M{"_id": player.ID()}, playerDoc)
	if err != nil {
		return fmt.Errorf("saving player in db: %w", err)
	}

	return nil
}

func toDocument(player *domain.Player) *PlayerDocument {
	return &PlayerDocument{
		ID:      player.ID(),
		GroupID: player.GroupID,
		UserID:  player.UserID,
		Role:    int(player.Role),
	}
}

func toPlayer(playerDoc *PlayerDocument) *domain.Player {
	return &domain.Player{
		Aggregate: ddd.NewAggregate(playerDoc.ID, domain.PlayerAggregate),
		GroupID:   playerDoc.GroupID,
		UserID:    playerDoc.UserID,
		Role:      domain.PlayerRole(playerDoc.Role),
	}
}
