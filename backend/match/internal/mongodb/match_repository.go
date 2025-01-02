package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

const timeout = 10 * time.Second

type MatchDocument struct {
	ID            string                 `bson:"_id,omitempty"`
	GroupID       string                 `bson:"groupId,omitempty"`
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

func (g MatchRepository) FindByID(id string) (*domain.Match, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	matchDoc := MatchDocument{}
	err := g.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&matchDoc)
	if err != nil {
		return nil, fmt.Errorf("finding match %s: %w", id, err)
	}

	result, err := toDomain(&matchDoc)
	if err != nil {
		return nil, fmt.Errorf("converting match %s: %w", id, err)
	}

	return result, nil
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
		GroupID:       match.GroupID(),
		Begin:         match.Begin().Unix(),
		Location:      match.Location().Name(),
		PlayerMax:     match.PlayerCount().Min(),
		PlayerMin:     match.PlayerCount().Min(),
		Registrations: registrations,
	}
}

func toDomain(matchDoc *MatchDocument) (*domain.Match, error) {
	registrations := make([]*domain.Registration, 0, len(matchDoc.Registrations))
	for _, r := range matchDoc.Registrations {
		registrations = append(registrations, domain.NewRegistration(
			r.UserID,
			domain.RegistrationStatusFromString(r.Status),
			time.Unix(r.TimeStamp, 0),
		))
	}

	location, err := domain.NewLocation(matchDoc.Location)
	if err != nil {
		return nil, fmt.Errorf("invalid location %s: %w", matchDoc.Location, err)
	}

	playerCount, err := domain.NewPlayerCount(matchDoc.PlayerMin, matchDoc.PlayerMax)
	if err != nil {
		return nil, fmt.Errorf("invalid player count %d-%d: %w", matchDoc.PlayerMin, matchDoc.PlayerMax, err)
	}

	return domain.NewMatch(
		matchDoc.ID,
		matchDoc.GroupID,
		time.Unix(matchDoc.Begin, 0),
		location,
		playerCount,
		registrations,
	), nil
}
