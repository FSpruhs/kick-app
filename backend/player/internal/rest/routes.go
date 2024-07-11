package rest

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/gin-gonic/gin"
)

func PlayerRoutes(router *gin.Engine, app application.App) {
	router.GET("/player")
}
