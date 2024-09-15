package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/player/internal/application"
)

func PlayerRoutes(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	{
		api.GET("/player")
	}
}
