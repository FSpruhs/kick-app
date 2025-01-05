package rest

import (
	"github.com/FSpruhs/kick-app/backend/internal/ginconfig"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/createuser"
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/getuserall"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/getusermessages"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/loginuser"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/messageread"
)

func UserRoutes(router *gin.Engine, app application.App) {
	router.POST("/api/v1/user", createuser.Handle(app))
	api := router.Group("/api/v1")
	api.Use(ginconfig.JWTValidator())
	api.Use(ginconfig.UserIDExtractor())
	{
		api.GET("/user", getuserall.Handle(app))
		api.POST("/user/login", loginuser.Handle(app))
		api.PUT("/message/read", messageread.Handle(app))
		api.GET("/message/:userId", getusermessages.Handle(app))
	}
}
