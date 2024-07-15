package grpc

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
	"google.golang.org/grpc"
)

type PlayerRepository struct {
	client playerspb.PlayersServiceClient
}

var _ domain.PlayerRepository = (*PlayerRepository)(nil)

func NewPlayerRepository(conn *grpc.ClientConn) *PlayerRepository {
	return &PlayerRepository{client: playerspb.NewPlayersServiceClient(conn)}
}

func (r *PlayerRepository) ConfirmPlayer(playerID string) error {
	_, err := r.client.ConfirmPlayer(context.Background(), &playerspb.ConfirmPlayerRequest{PlayerId: playerID})
	return err
}
