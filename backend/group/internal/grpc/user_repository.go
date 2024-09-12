package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/user/userpb"
)

type UserRepository struct {
	client userpb.UserServiceClient
}

var _ domain.UserRepository = (*UserRepository)(nil)

func NewUserRepository(conn *grpc.ClientConn) *UserRepository {
	return &UserRepository{client: userpb.NewUserServiceClient(conn)}
}

func (r *UserRepository) GetUser(userID string) (*domain.User, error) {
	resp, err := r.client.GetUser(context.Background(), &userpb.GetUserRequest{UserId: userID})
	if err != nil {
		return nil, fmt.Errorf("get user %s: %w", userID, err)
	}

	return domain.NewUser(resp.GetUserId(), resp.GetNickName()), nil
}

func (r *UserRepository) GetUserAll(userIDs []string) ([]*domain.User, error) {
	resp, err := r.client.GetUserAll(context.Background(), &userpb.GetUserAllRequest{UserIds: userIDs})
	if err != nil {
		return nil, fmt.Errorf("get users %v: %w", userIDs, err)
	}

	users := make([]*domain.User, len(resp.GetUsers()))
	for i, u := range resp.GetUsers() {
		users[i] = domain.NewUser(u.GetUserId(), u.GetNickName())
	}

	return users, nil
}
