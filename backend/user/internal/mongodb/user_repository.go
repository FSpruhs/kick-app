package mongodb

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"time"
)

type UserDocument struct {
	ID        primitive.ObjectID `bson:"_id,omitempty"`
	FirstName string             `json:"firstName,omitempty"`
	LastName  string             `json:"lastName,omitempty"`
	NickName  string             `json:"nickName,omitempty"`
	Email     string             `json:"email,omitempty"`
	Password  string             `json:"password,omitempty"`
	Groups    []string           `json:"groups,omitempty"`
}

type UserRepository struct {
	collection *mongo.Collection
}

var _ domain.UserRepository = (*UserRepository)(nil)

func NewUserRepository(database *mongo.Database, collectionName string) UserRepository {
	collection := database.Collection(collectionName)
	indexModel := mongo.IndexModel{
		Keys:    bson.M{"email": 1},
		Options: options.Index().SetUnique(true),
	}
	_, err := collection.Indexes().CreateOne(context.Background(), indexModel)
	if err != nil {
		log.Fatalf("could not create index: %v", err)
	}

	return UserRepository{collection: collection}
}

func (u UserRepository) Create(newUser *domain.User) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	userDoc := UserDocument{
		ID:        primitive.NewObjectID(),
		FirstName: newUser.FullName.FirstName(),
		LastName:  newUser.FullName.LastName(),
		NickName:  newUser.NickName,
		Email:     newUser.Email.Value(),
		Password:  newUser.Password.Hash(),
		Groups:    newUser.Groups,
	}
	result, err := u.collection.InsertOne(ctx, userDoc)
	if id, ok := result.InsertedID.(primitive.ObjectID); ok {
		newUser.SetId(id.Hex())
	}

	return newUser, err
}

func (u UserRepository) CountByEmail(email *domain.Email) (int, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"email": email.Value()}

	count, err := u.collection.CountDocuments(ctx, filter)
	if err != nil {
		log.Fatalln(err)
		return 0, err
	}

	return int(count), nil
}

func (u UserRepository) FindByEmail(email *domain.Email) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	filter := bson.M{"email": email.Value()}

	var userDoc UserDocument
	err := u.collection.FindOne(ctx, filter).Decode(&userDoc)
	if err != nil {
		return nil, err
	}

	user, err := toDomain(userDoc)
	if err != nil {
		return nil, err
	}
	return user, nil
}

func toDomain(userDoc UserDocument) (*domain.User, error) {
	fullName, err := domain.NewFullName(userDoc.FirstName, userDoc.LastName)
	if err != nil {
		return nil, domain.ErrInvalidFullName
	}

	email, err := domain.NewEmail(userDoc.Email)
	if err != nil {
		log.Fatalln("email is invalid")
		return nil, domain.ErrEmailInvalid

	}
	password := domain.NewHashedPassword(userDoc.Password)
	return &domain.User{
		Id:       userDoc.ID.Hex(),
		FullName: fullName,
		NickName: userDoc.NickName,
		Email:    email,
		Password: password,
		Groups:   userDoc.Groups,
	}, nil
}
