package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/creategroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/getgroups"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteduserresponse"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteuser"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/leavegroup"
)

func GroupRouter(router *gin.Engine, app application.App) {
	api := router.Group("/api/v1")
	{
		api.POST("/group", creategroup.Handle(app))
		api.GET("/group/:userId", getgroups.Handle(app))
		api.POST("/group/user", inviteuser.Handle(app))
		api.PUT("/group/user", inviteduserresponse.Handle(app))
		api.DELETE("/group/user", leavegroup.Handle(app))
	}
}
