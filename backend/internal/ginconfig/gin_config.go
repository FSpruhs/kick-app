package ginconfig

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func CorsMiddleware() gin.HandlerFunc {
	return func(context *gin.Context) {
		context.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		context.Writer.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		context.Writer.Header().Set("Access-Control-Allow-Headers", "Origin, Authorization, Content-Type")
		context.Writer.Header().Set("Access-Control-Allow-Credentials", "true")

		if context.Request.Method == http.MethodOptions {
			context.AbortWithStatus(http.StatusOK)

			return
		}

		context.Next()
	}
}
