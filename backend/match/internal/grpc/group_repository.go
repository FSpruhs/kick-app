package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

type GroupRepository struct {
	client grouppb.GroupServiceClient
}

var _ domain.GroupRepository = (*GroupRepository)(nil)

func NewGroupRepository(conn *grpc.ClientConn) *GroupRepository {
	return &GroupRepository{client: grouppb.NewGroupServiceClient(conn)}
}

func (r *GroupRepository) IsPlayerActive(userID, groupID string) (bool, error) {
	resp, err := r.client.IsActivePlayer(
		context.Background(),
		&grouppb.IsActivePlayerRequest{UserId: userID, GroupId: groupID},
	)
	if err != nil {
		return false, fmt.Errorf("is player active %s %s: %w", userID, groupID, err)
	}

	return resp.IsActive, nil
}
