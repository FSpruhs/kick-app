package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/rest/controller/updateplayerrole"
)

func PlayerRoutes(router *gin.Engine, app application.App) {
	router.GET("/player")
	router.PUT("/player/role", updateplayerrole.Handle(app))
}
