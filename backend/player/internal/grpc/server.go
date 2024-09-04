package grpc

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
)

type server struct {
	app application.App
	playerspb.UnimplementedPlayersServiceServer
}

var _ playerspb.PlayersServiceServer = (*server)(nil)

func RegisterServer(app application.App, registrar grpc.ServiceRegistrar) error {
	playerspb.RegisterPlayersServiceServer(registrar, &server{app: app})

	return nil
}

func (s server) ConfirmPlayer(
	_ context.Context,
	request *playerspb.ConfirmPlayerRequest,
) (*playerspb.ConfirmPlayerResponse, error) {
	if err := s.app.ConfirmPlayer(&commands.ConfirmPlayer{
		PlayerID:    request.PlayerId,
		GroupID:     request.GroupId,
		InviteLevel: int(request.InviteLevel),
	}); err != nil {
		return nil, fmt.Errorf("while confirming player: %w", err)
	}

	return &playerspb.ConfirmPlayerResponse{}, nil
}
