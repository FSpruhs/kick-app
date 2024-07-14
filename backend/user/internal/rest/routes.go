package rest

import (
	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/createuser"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/loginuser"
	"github.com/gin-gonic/gin"
)

func UserRoutes(router *gin.Engine, app application.App) {
	router.POST("/user", createuser.Handle(app))
	router.GET("/user")
	router.POST("/user/login", loginuser.Handle(app))
}
