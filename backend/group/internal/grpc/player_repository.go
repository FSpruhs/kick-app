package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
)

type PlayerRepository struct {
	client playerspb.PlayersServiceClient
}

var _ domain.PlayerRepository = (*PlayerRepository)(nil)

func NewPlayerRepository(conn *grpc.ClientConn) *PlayerRepository {
	return &PlayerRepository{client: playerspb.NewPlayersServiceClient(conn)}
}

func (r *PlayerRepository) ConfirmPlayer(playerID, groupID string, inviteLevel int) error {
	_, err := r.client.ConfirmPlayer(context.Background(), &playerspb.ConfirmPlayerRequest{
		PlayerId:    playerID,
		GroupId:     groupID,
		InviteLevel: int32(inviteLevel),
	})

	return fmt.Errorf("failed to confirm player: %w", err)
}

func (r *PlayerRepository) ConfirmUserLeavingGroup(userID, groupID string) error {
	_, err := r.client.ConfirmGroupLeavingUser(context.Background(), &playerspb.ConfirmGroupLeavingUserRequest{
		UserId:  userID,
		GroupId: groupID,
	})

	return fmt.Errorf("failed to confirm user leaving group: %w", err)
}
