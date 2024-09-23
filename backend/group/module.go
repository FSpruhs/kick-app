package group

import (
	"fmt"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/grpc"
	"github.com/FSpruhs/kick-app/backend/group/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
	groups := mongodb.NewGroupRepository(mono.DB(), "group.groups")

	conn, err := grpc.NewClient(mono.Config().RPC.Address())
	if err != nil {
		return fmt.Errorf("connect to rpc server: %w", err)
	}

	users := grpc.NewUserRepository(conn)

	app := application.New(groups, users, mono.EventDispatcher())

	rest.GroupRouter(mono.Router(), app)

	return nil
}
