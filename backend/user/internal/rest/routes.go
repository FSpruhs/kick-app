package rest

import (
	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/createUser"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/loginUser"
	"github.com/gin-gonic/gin"
)

func UserRoutes(router *gin.Engine, app application.App) {
	router.POST("/user", createUser.Handle(app))
	router.GET("/user")
	router.POST("/user/login", loginUser.Handle(app))
}
