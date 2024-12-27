package mongodb

import "go.mongodb.org/mongo-driver/mongo"

type MatchRepository struct {
	collection *mongo.Collection
}

func NewMatchRepository(db *mongo.Database, collectionName string) *MatchRepository {
	return &MatchRepository{collection: db.Collection(collectionName)}
}
