package mongodb

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type UserDocument struct {
	ID        string   `bson:"_id,omitempty"`
	FirstName string   `json:"firstName,omitempty"`
	LastName  string   `json:"lastName,omitempty"`
	NickName  string   `json:"nickName,omitempty"`
	Email     string   `json:"email,omitempty"`
	Password  string   `json:"password,omitempty"`
	Groups    []string `json:"groups,omitempty"`
}

type UserRepository struct {
	collection *mongo.Collection
}

var _ domain.UserRepository = (*UserRepository)(nil)

func NewUserRepository(database *mongo.Database, collectionName string) (*UserRepository, error) {
	collection := database.Collection(collectionName)
	indexModel := mongo.IndexModel{
		Keys:    bson.M{"email": 1},
		Options: options.Index().SetUnique(true),
	}

	_, err := collection.Indexes().CreateOne(context.Background(), indexModel)
	if err != nil {
		return nil, fmt.Errorf("creating email as user index: %w", err)
	}

	return &UserRepository{collection: collection}, nil
}

func (u UserRepository) Create(newUser *domain.User) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	userDoc := UserDocument{
		ID:        newUser.ID,
		FirstName: newUser.FullName.FirstName(),
		LastName:  newUser.FullName.LastName(),
		NickName:  newUser.NickName,
		Email:     newUser.Email.Value(),
		Password:  newUser.Password.Hash(),
		Groups:    newUser.Groups,
	}

	_, err := u.collection.InsertOne(ctx, userDoc)
	if err != nil {
		return nil, fmt.Errorf("inserting user: %w", err)
	}

	return newUser, nil
}

func (u UserRepository) CountByEmail(email *domain.Email) (int, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"email": email.Value()}

	count, err := u.collection.CountDocuments(ctx, filter)
	if err != nil {
		return 0, fmt.Errorf("counting users by email: %w", err)
	}

	return int(count), nil
}

func (u UserRepository) FindByEmail(email *domain.Email) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"email": email.Value()}

	var userDoc UserDocument

	if err := u.collection.FindOne(ctx, filter).Decode(&userDoc); err != nil {
		return nil, fmt.Errorf("finding user by email: %w", err)
	}

	user, err := toDomain(&userDoc)
	if err != nil {
		return nil, fmt.Errorf("converting user document to domain: %w", err)
	}

	return user, nil
}

func (u UserRepository) FindByID(id string) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"_id": id}

	var userDoc UserDocument

	if err := u.collection.FindOne(ctx, filter).Decode(&userDoc); err != nil {
		return nil, fmt.Errorf("finding user by id: %w", err)
	}

	user, err := toDomain(&userDoc)
	if err != nil {
		return nil, fmt.Errorf("converting user document to domain: %w", err)
	}

	return user, nil
}

func (u UserRepository) FindByIDs(ids []string) ([]*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"_id": bson.M{"$in": ids}}

	cursor, err := u.collection.Find(ctx, filter)
	if err != nil {
		return nil, fmt.Errorf("finding users by ids: %w", err)
	}
	defer cursor.Close(ctx)

	var userDocs []UserDocument

	if err := cursor.All(ctx, &userDocs); err != nil {
		return nil, fmt.Errorf("iterating over users: %w", err)
	}

	users := make([]*domain.User, len(userDocs))

	for index, userDoc := range userDocs {
		user, err := toDomain(&userDoc)
		if err != nil {
			return nil, fmt.Errorf("converting user document to domain: %w", err)
		}

		users[index] = user
	}

	return users, nil
}

func toDomain(userDoc *UserDocument) (*domain.User, error) {
	fullName, err := domain.NewFullName(userDoc.FirstName, userDoc.LastName)
	if err != nil {
		return nil, fmt.Errorf("creating fullname: %w", err)
	}

	email, err := domain.NewEmail(userDoc.Email)
	if err != nil {
		return nil, fmt.Errorf("creating email: %w", err)
	}

	password := domain.NewHashedPassword(userDoc.Password)

	return &domain.User{
		ID:       userDoc.ID,
		FullName: fullName,
		NickName: userDoc.NickName,
		Email:    email,
		Password: password,
		Groups:   userDoc.Groups,
	}, nil
}
