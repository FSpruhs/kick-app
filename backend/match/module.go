package match

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/grpc"
	"github.com/FSpruhs/kick-app/backend/match/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/match/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	matches := mongodb.NewMatchRepository(mono.DB(), "match.matches")

	conn, err := grpc.NewClient(mono.Config().RPC.Address())
	if err != nil {
		return fmt.Errorf("connect to rpc server: %w", err)
	}

	groups := grpc.NewGroupRepository(conn)

	app := application.New(matches, groups)

	rest.MatchRoutes(mono.Router(), app)

	return nil
}
