package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest/controller/updateplayerrole"
)

func PlayerRoutes(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	{
		api.GET("/player")
		api.PUT("/player/role", updateplayerrole.Handle(app))
	}
}
