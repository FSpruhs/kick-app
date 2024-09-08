package main

import (
	"context"
	"fmt"
	"github.com/FSpruhs/kick-app/backend/cmd/docs"
	"net"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
	"golang.org/x/sync/errgroup"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"github.com/FSpruhs/kick-app/backend/group"
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/internal/ginconfig"
	"github.com/FSpruhs/kick-app/backend/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/internal/rpc"
	"github.com/FSpruhs/kick-app/backend/internal/waiter"
	"github.com/FSpruhs/kick-app/backend/player"
	"github.com/FSpruhs/kick-app/backend/user"
	"github.com/swaggo/files"
	"github.com/swaggo/gin-swagger"
)

type app struct {
	cfg             config.AppConfig
	modules         []monolith.Module
	db              *mongo.Database
	router          *gin.Engine
	eventDispatcher *ddd.EventDispatcher[ddd.AggregateEvent]
	rpc             *grpc.Server
	waiter          waiter.Waiter
}

var _ monolith.Monolith = (*app)(nil)

func (a *app) Config() config.AppConfig {
	return a.cfg
}

func (a *app) RPC() *grpc.Server {
	return a.rpc
}

func (a *app) DB() *mongo.Database {
	return a.db
}

func (a *app) Router() *gin.Engine {
	return a.router
}

func (a *app) EventDispatcher() *ddd.EventDispatcher[ddd.AggregateEvent] {
	return a.eventDispatcher
}

func (a *app) Waiter() waiter.Waiter {
	return a.waiter
}

func (a *app) waitForWeb(ctx context.Context) error {
	ginGroup, gCtx := errgroup.WithContext(ctx)
	ginGroup.Go(func() error {
		if err := a.router.Run(); err != nil {
			return err
		}
		return nil
	})
	ginGroup.Go(func() error {
		<-gCtx.Done()
		_, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		return nil
	})

	return ginGroup.Wait()
}

func (a *app) waitForRpc(ctx context.Context) error {
	listener, err := net.Listen("tcp", a.cfg.RPC.Address())
	if err != nil {
		return err
	}

	grpcGroup, gCtx := errgroup.WithContext(ctx)
	grpcGroup.Go(func() error {
		fmt.Println("rpc server started")
		defer fmt.Println("rpc server stopped")
		if err := a.RPC().Serve(listener); err != nil {
			return err
		}

		return nil
	})

	grpcGroup.Go(func() error {
		<-gCtx.Done()
		fmt.Println("shutting down rpc server")
		stopped := make(chan struct{})
		go func() {
			a.RPC().GracefulStop()
			close(stopped)
		}()
		timeout := time.NewTimer(5 * time.Second)
		select {
		case <-timeout.C:
			a.RPC().Stop()
			return fmt.Errorf("rpc server failed to stop gracefully")

		case <-stopped:
			return nil
		}
	})

	return grpcGroup.Wait()
}

func main() {
	if err := run(); err != nil {
		fmt.Println(err.Error())
		os.Exit(1)
	}
}

func run() error {
	conf := config.InitConfig()

	db := mongodb.ConnectMongoDB(conf.EnvMongoURI, conf.DatabaseName)

	eventDispatcher := ddd.NewEventDispatcher[ddd.AggregateEvent]()
	newWaiter := waiter.New(waiter.CatchSignals())
	router := initRouter()
	newRpc := initRpc(conf.RPC)

	modules := []monolith.Module{
		&player.Module{},
		&user.Module{},
		&group.Module{},
	}

	m := app{
		cfg:             conf,
		modules:         modules,
		db:              db,
		router:          router,
		eventDispatcher: eventDispatcher,
		rpc:             newRpc,
		waiter:          newWaiter,
	}
	m.startupModules()

	fmt.Println("Started KickApp")
	defer fmt.Println("Stopped KickApp")

	m.waiter.Add(
		m.waitForWeb,
		m.waitForRpc,
	)

	return m.waiter.Wait()

}

func (a *app) startupModules() {
	for _, module := range a.modules {
		if err := module.Startup(a); err != nil {
			panic(err)
		}
	}
}

func initRpc(_ rpc.Config) *grpc.Server {
	server := grpc.NewServer()
	reflection.Register(server)

	return server
}

func initRouter() *gin.Engine {
	router := gin.Default()
	router.Use(ginconfig.CorsMiddleware())

	docs.SwaggerInfo.BasePath = "/api/v1"
	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	return router
}
