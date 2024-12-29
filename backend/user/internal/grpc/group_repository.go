package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type GroupRepository struct {
	client grouppb.GroupServiceClient
}

var _ domain.GroupRepository = (*GroupRepository)(nil)

func NewGroupRepository(conn *grpc.ClientConn) *GroupRepository {
	return &GroupRepository{client: grouppb.NewGroupServiceClient(conn)}
}

func (r *GroupRepository) FindPlayersByGroup(groupID string) ([]string, error) {
	resp, err := r.client.GetActivePlayersByGroupID(
		context.Background(),
		&grouppb.GetActivePlayersByGroupIDRequest{GroupId: groupID},
	)
	if err != nil {
		return nil, fmt.Errorf("get active players by group id: %w", err)
	}

	return resp.GetUserIds(), nil
}
