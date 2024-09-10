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
		PlayerID:    request.GetPlayerId(),
		GroupID:     request.GetGroupId(),
		InviteLevel: int(request.GetInviteLevel()),
	}); err != nil {
		return nil, fmt.Errorf("confirm player: %w", err)
	}

	return &playerspb.ConfirmPlayerResponse{}, nil
}

func (s server) ConfirmGroupLeavingUser(
	_ context.Context,
	request *playerspb.ConfirmGroupLeavingUserRequest,
) (*playerspb.ConfirmGroupLeavingUserResponse, error) {
	command := commands.ConfirmGroupLeavingUser{
		UserID:  request.GetUserId(),
		GroupID: request.GetGroupId(),
	}

	if err := s.app.ConfirmGroupLeavingUser(&command); err != nil {
		return nil, fmt.Errorf("confirm user leaving group: %w", err)
	}

	return &playerspb.ConfirmGroupLeavingUserResponse{}, nil
}
