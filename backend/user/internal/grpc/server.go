package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/user/userpb"
)

type server struct {
	app application.App
	userpb.UnimplementedUserServiceServer
}

var _ userpb.UserServiceServer = (*server)(nil)

func RegisterServer(app application.App, registrar grpc.ServiceRegistrar) error {
	userpb.RegisterUserServiceServer(registrar, &server{app: app})

	return nil
}

func (s server) GetUser(_ context.Context, request *userpb.GetUserRequest) (*userpb.GetUserResponse, error) {
	query := &queries.GetUser{UserID: request.GetUserId()}

	user, err := s.app.GetUser(query)
	if err != nil {
		return nil, fmt.Errorf("get user: %w", err)
	}

	return &userpb.GetUserResponse{UserId: user.ID, NickName: user.NickName}, nil
}

func (s server) GetUserAll(_ context.Context, request *userpb.GetUserAllRequest) (*userpb.GetUserAllResponse, error) {
	query := &queries.GetUserAll{UserIDs: request.GetUserIds()}

	user, err := s.app.GetUserAll(query)
	if err != nil {
		return nil, fmt.Errorf("get user all: %w", err)
	}

	users := make([]*userpb.User, len(user))
	for index, u := range user {
		users[index] = &userpb.User{UserId: u.ID, NickName: u.NickName}
	}

	response := &userpb.GetUserAllResponse{Users: users}

	return response, nil
}
