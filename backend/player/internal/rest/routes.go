package rest

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest/controller/createPlayer"
	"github.com/gin-gonic/gin"
)

func PlayerRoutes(router *gin.Engine, app application.App) {
	router.POST("/player", createPlayer.Handle(app))
	router.GET("/player")
}
