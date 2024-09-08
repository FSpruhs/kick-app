package rest

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/getgroups"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteduserresponse"
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/creategroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteuser"
)

func GroupRouter(router *gin.Engine, app application.App) {
	router.POST("/group", creategroup.Handle(app))
	router.GET("/group/:userId", getgroups.Handle(app))
	router.POST("/group/user", inviteuser.Handle(app))
	router.PUT("/group/user", inviteduserresponse.Handle(app))
}
