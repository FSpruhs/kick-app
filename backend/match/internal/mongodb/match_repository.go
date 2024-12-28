package mongodb

import (
	"context"
	"fmt"
	"time"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

const timeout = 10 * time.Second

type MatchDocument struct {
	ID            string                 `bson:"_id,omitempty"`
	Begin         int64                  `bson:"begin,omitempty"`
	Location      string                 `bson:"location,omitempty"`
	PlayerMax     int                    `bson:"playerMax,omitempty"`
	PlayerMin     int                    `bson:"playerMin,omitempty"`
	Registrations []RegistrationDocument `bson:"registrations,omitempty"`
}

type RegistrationDocument struct {
	UserID    string `bson:"userId,omitempty"`
	Status    string `bson:"status,omitempty"`
	TimeStamp int64  `bson:"timeStamp,omitempty"`
}

type MatchRepository struct {
	collection *mongo.Collection
}

var _ domain.MatchRepository = (*MatchRepository)(nil)

func NewMatchRepository(db *mongo.Database, collectionName string) *MatchRepository {
	return &MatchRepository{collection: db.Collection(collectionName)}
}

func (g MatchRepository) Save(match *domain.Match) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	matchDoc := toDocument(match)

	_, err := g.collection.ReplaceOne(ctx, bson.M{"_id": match.ID()}, matchDoc)
	if err != nil {
		return fmt.Errorf("saving match %s: %w", match.ID(), err)
	}

	return nil
}

func toDocument(match *domain.Match) MatchDocument {
	registrations := make([]RegistrationDocument, 0, len(match.Registrations()))
	for _, r := range match.Registrations() {
		registrations = append(registrations, RegistrationDocument{
			UserID:    r.UserID(),
			Status:    r.Status().String(),
			TimeStamp: r.TimeStamp().Unix(),
		})
	}

	return MatchDocument{
		ID:            match.ID(),
		Begin:         match.Begin().Unix(),
		Location:      match.Location().Name(),
		PlayerMax:     match.PlayerCount().Min(),
		PlayerMin:     match.PlayerCount().Min(),
		Registrations: registrations,
	}
}
