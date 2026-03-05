package main

import (
	"context"
	"fmt"
	"log"
	"net"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"go.mongodb.org/mongo-driver/mongo"
	"golang.org/x/sync/errgroup"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"github.com/FSpruhs/kick-app/backend/cmd/docs"
	"github.com/FSpruhs/kick-app/backend/group"
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/internal/ginconfig"
	"github.com/FSpruhs/kick-app/backend/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/internal/rpc"
	"github.com/FSpruhs/kick-app/backend/internal/waiter"
	"github.com/FSpruhs/kick-app/backend/match"
	"github.com/FSpruhs/kick-app/backend/player"
	"github.com/FSpruhs/kick-app/backend/user"
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
	const timeoutDuration = 5 * time.Second

	ginGroup, gCtx := errgroup.WithContext(ctx)

	ginGroup.Go(func() error {
		if err := a.router.Run(":" + a.Config().Gin.Port); err != nil {
			return fmt.Errorf("starting web server: %w", err)
		}

		return nil
	})
	ginGroup.Go(func() error {
		<-gCtx.Done()

		_, cancel := context.WithTimeout(context.Background(), timeoutDuration)
		defer cancel()

		return nil
	})

	return ginGroup.Wait()
}

func (a *app) waitForRPC(ctx context.Context) error {
	listener, err := net.Listen("tcp", a.cfg.RPC.Address())
	if err != nil {
		return fmt.Errorf("listen to %s: %w", a.cfg.RPC.Address(), err)
	}

	grpcGroup, gCtx := errgroup.WithContext(ctx)
	grpcGroup.Go(func() error {
		log.Println("rpc server started")
		defer log.Println("rpc server stopped")

		if err := a.RPC().Serve(listener); err != nil {
			return fmt.Errorf("rpc server starts listening: %w", err)
		}

		return nil
	})

	grpcGroup.Go(func() error {
		<-gCtx.Done()
		log.Println("shutting down rpc server")

		const timeoutDuration = 5 * time.Second

		stopped := make(chan struct{})
		go func() {
			a.RPC().GracefulStop()
			close(stopped)
		}()

		timeout := time.NewTimer(timeoutDuration)
		select {
		case <-timeout.C:
			a.RPC().Stop()

			return fmt.Errorf("rpc server failed to stop gracefully: %w", ctx.Err())

		case <-stopped:
			return nil
		}
	})

	return grpcGroup.Wait()
}

func main() {
	if err := run(); err != nil {
		log.Println(err.Error())
		os.Exit(1)
	}
}

func run() error {
	conf := config.InitConfig()

	mongoDB := mongodb.ConnectMongoDB(conf.EnvMongoURI, conf.DatabaseName)

	eventDispatcher := ddd.NewEventDispatcher[ddd.AggregateEvent]()
	newWaiter := waiter.New(waiter.CatchSignals())
	router := initRouter()
	newRPC := initRPC(conf.RPC)

	modules := []monolith.Module{
		&player.Module{},
		&user.Module{},
		&group.Module{},
		&match.Module{},
	}

	application := app{
		cfg:             conf,
		modules:         modules,
		db:              mongoDB,
		router:          router,
		eventDispatcher: eventDispatcher,
		rpc:             newRPC,
		waiter:          newWaiter,
	}
	application.startupModules()

	log.Println("Started KickApp")
	defer log.Println("Stopped KickApp")

	application.waiter.Add(
		application.waitForWeb,
		application.waitForRPC,
	)

	return application.waiter.Wait()
}

func (a *app) startupModules() {
	for _, module := range a.modules {
		if err := module.Startup(a); err != nil {
			panic(err)
		}
	}
}

func initRPC(_ rpc.Config) *grpc.Server {
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
