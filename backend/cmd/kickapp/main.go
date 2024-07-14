package main

import (
	"github.com/FSpruhs/kick-app/backend/group"
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/internal/ginConfig"
	"github.com/FSpruhs/kick-app/backend/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player"
	"github.com/FSpruhs/kick-app/backend/user"
	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
)

type app struct {
	cfg             config.AppConfig
	modules         []monolith.Module
	db              *mongo.Database
	router          *gin.Engine
	eventDispatcher *ddd.EventDispatcher[ddd.AggregateEvent]
}

func (a *app) Config() config.AppConfig {
	return a.cfg
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

func main() {
	conf := config.InitConfig()

	db := mongodb.ConnectMongoDB(conf.EnvMongoURI, conf.DatabaseName)

	router := gin.Default()
	router.Use(ginConfig.CorsMiddleware())
	eventDispatcher := ddd.NewEventDispatcher[ddd.AggregateEvent]()

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
	}
	m.startupModules()

	m.router.Run()
}

func (a *app) startupModules() {
	for _, module := range a.modules {
		module.Startup(a)
	}
}
