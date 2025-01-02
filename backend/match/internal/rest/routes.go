package rest

import (
	"github.com/FSpruhs/kick-app/backend/match/internal/rest/controller/removeregistration"
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/rest/controller/addregistration"
	"github.com/FSpruhs/kick-app/backend/match/internal/rest/controller/creatematch"
	"github.com/FSpruhs/kick-app/backend/match/internal/rest/controller/invitationresponse"
)

func MatchRoutes(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	{
		api.POST("/match", creatematch.Handle(app))
		api.POST("/match/registration", invitationresponse.Handle(app))
		api.PUT("/match/registration", addregistration.Handle(app))
		api.DELETE("/match/registration", removeregistration.Handle(app))
	}

}
