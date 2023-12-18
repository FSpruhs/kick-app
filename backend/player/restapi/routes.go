package restapi

import (
	"github.com/FSpruhs/kick-app/backend/player/persistence"
	"github.com/FSpruhs/kick-app/backend/player/restapi/controller/createPlayer"
	"github.com/gin-gonic/gin"
)

func PlayerRoutes(router *gin.Engine, repository persistence.PlayerRepository) {
	router.POST("/player", createPlayer.Handle(repository))
	router.GET("/player")
}
