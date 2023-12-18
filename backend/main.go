package main

import (
	"github.com/FSpruhs/kick-app/backend/config"
	"github.com/FSpruhs/kick-app/backend/player/persistence/playerRepository"
	"github.com/FSpruhs/kick-app/backend/player/restapi"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"log"
)

func main() {
	router := gin.Default()

	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env.file")
	}

	mongoClient := config.ConnectMongoDB()

	restapi.PlayerRoutes(router, playerRepository.New(mongoClient))
	router.Run()
}
