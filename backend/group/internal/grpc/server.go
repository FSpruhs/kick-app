package grpc

import (
	"context"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/queries"
)

type server struct {
	app application.App
	grouppb.UnimplementedGroupServiceServer
}

var _ grouppb.GroupServiceServer = (*server)(nil)

func RegisterServer(app application.App, registrar grpc.ServiceRegistrar) error {
	grouppb.RegisterGroupServiceServer(registrar, &server{app: app})

	return nil
}

func (s server) IsActivePlayer(_ context.Context, request *grouppb.IsActivePlayerRequest) (*grouppb.IsActivePlayerResponse, error) {
	query := &queries.IsPlayerActive{UserID: request.GetUserId(), GroupID: request.GetGroupId()}
	result := s.app.IsPlayerActive(query)

	return &grouppb.IsActivePlayerResponse{IsActive: result}, nil
}
