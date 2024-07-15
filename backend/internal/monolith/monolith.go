package monolith

import (
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/internal/waiter"
	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
	"google.golang.org/grpc"
)

type Monolith interface {
	Config() config.AppConfig
	DB() *mongo.Database
	Router() *gin.Engine
	EventDispatcher() *ddd.EventDispatcher[ddd.AggregateEvent]
	RPC() *grpc.Server
	Waiter() waiter.Waiter
}

type Module interface {
	Startup(m Monolith) error
}
