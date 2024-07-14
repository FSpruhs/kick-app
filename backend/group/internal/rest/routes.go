package rest

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/creategroup"
	"github.com/FSpruhs/kick-app/backend/group/internal/rest/controller/inviteuser"
	"github.com/gin-gonic/gin"
)

func GroupRouter(router *gin.Engine, app application.App) {
	router.POST("/group", creategroup.Handle(app))
	router.POST("/group/user", inviteuser.Handle(app))
}
