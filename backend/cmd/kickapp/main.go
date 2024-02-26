package main

import (
	"github.com/FSpruhs/kick-app/backend/internal/config"
	"github.com/FSpruhs/kick-app/backend/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/internal/monolith"
	"github.com/FSpruhs/kick-app/backend/player"
	"go.mongodb.org/mongo-driver/mongo"

	"github.com/gin-gonic/gin"
)

type app struct {
	cfg     config.AppConfig
	modules []monolith.Module
	db      *mongo.Database
	router  *gin.Engine
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

func main() {
	var conf = config.InitConfig()
	m := app{cfg: conf}

	m.db = mongodb.ConnectMongoDB(conf.EnvMongoURI, conf.DatabaseName)

	m.router = gin.Default()

	m.modules = []monolith.Module{
		&player.Module{},
	}

	m.startupModules()

	//rest.PlayerRoutes(router, playerRepository.New(mongoClient))
	m.router.Run()
}

func (a *app) startupModules() {
	for _, module := range a.modules {
		module.Startup(a)
	}
}
