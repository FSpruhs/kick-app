package rest

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/createGroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteUser"
	"github.com/gin-gonic/gin"
)

func GroupRouter(router *gin.Engine, app application.App) {
	router.POST("/group", createGroup.Handle(app))
	router.POST("/group/user", inviteUser.Handle(app))
}
