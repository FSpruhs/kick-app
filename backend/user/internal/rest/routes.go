package rest

import (
	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/createuser"
	"github.com/FSpruhs/kick-app/backend/user/internal/rest/controller/loginuser"
)

func UserRoutes(router *gin.Engine, app application.App) {
	router.POST("/user", createuser.Handle(app))
	router.GET("/user")
	router.POST("/user/login", loginuser.Handle(app))
}
