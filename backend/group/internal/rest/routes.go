package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/creategroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/getgroupdetails"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/getgroups"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteduserresponse"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteuser"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/leavegroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/removeuser"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/updateplayer"
	"github.com/FSpruhs/kick-app/backend/internal/ginconfig"
)

func GroupRouter(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	api.Use(ginconfig.JWTMiddleware())
	{
		api.POST("/group", creategroup.Handle(app))
		api.GET("/group/user/:userId", getgroups.Handle(app))
		api.POST("/group/user", inviteuser.Handle(app))
		api.PUT("/group/user", inviteduserresponse.Handle(app))
		api.DELETE("group/:groupId/user/:userId", leavegroup.Handle(app))
		api.GET("/group/:groupId", getgroupdetails.Handle(app))
		api.PUT("/group/player", updateplayer.Handle(app))
		api.PUT("/group/player/status", removeuser.Handle(app))
	}
}
