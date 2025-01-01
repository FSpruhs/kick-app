package grpc

import (
	"context"
	"fmt"

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

func (s server) IsActivePlayer(
	_ context.Context,
	request *grouppb.IsActivePlayerRequest,
) (*grouppb.IsActivePlayerResponse, error) {
	query := &queries.IsPlayerActive{UserID: request.GetUserId(), GroupID: request.GetGroupId()}
	result := s.app.IsPlayerActive(query)

	return &grouppb.IsActivePlayerResponse{IsActive: result}, nil
}

func (s server) GetActivePlayersByGroupID(
	_ context.Context,
	request *grouppb.GetActivePlayersByGroupIDRequest,
) (*grouppb.GetActivePlayersByGroupIDResponse, error) {
	query := &queries.GetActivePlayersByGroup{GroupID: request.GetGroupId()}

	result, err := s.app.GetActivePlayersByGroup(query)
	if err != nil {
		return nil, fmt.Errorf("get active players by group id: %w", err)
	}

	return &grouppb.GetActivePlayersByGroupIDResponse{UserIds: result}, nil
}

func (s server) HasPlayerAdminRole(
	_ context.Context,
	request *grouppb.HasPlayerAdminRoleRequest,
) (*grouppb.HasPlayerAdminRoleResponse, error) {
	query := &queries.HasPlayerAdminRole{UserID: request.GetUserId(), GroupID: request.GetGroupId()}
	result := s.app.HasPlayerAdminRole(query)

	return &grouppb.HasPlayerAdminRoleResponse{HasAdminRole: result}, nil
}
