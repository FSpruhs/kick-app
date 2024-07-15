package grpc

import (
	"context"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
	"google.golang.org/grpc"
	"log"
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

func (s server) ConfirmPlayer(ctx context.Context, request *playerspb.ConfirmPlayerRequest) (*playerspb.ConfirmPlayerResponse, error) {
	log.Printf(request.String())
	return nil, nil
}
