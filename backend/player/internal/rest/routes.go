package rest

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest/controller/createPlayer"
	"github.com/gin-gonic/gin"
)

func PlayerRoutes(router *gin.Engine, repository mongodb.PlayerRepository) {
	router.POST("/player", createPlayer.Handle(repository))
	router.GET("/player")
}
