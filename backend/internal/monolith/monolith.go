package monolith

import (
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
)

type Monolith interface {
	Config() config.AppConfig
	DB() *mongo.Database
	Router() *gin.Engine
}

type Module interface {
	Startup(m Monolith)
}
