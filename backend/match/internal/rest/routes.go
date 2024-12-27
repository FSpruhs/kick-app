package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/rest/controller/creatematch"
)

func MatchRoutes(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	{
		api.POST("/match", creatematch.Handle(app))
	}

}
