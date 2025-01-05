package ginconfig

import (
	"errors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

func UserIDExtractor() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, c.Error(errors.New("authorization header missing")))
			c.Abort()

			return
		}

		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		if tokenString == authHeader {
			c.JSON(http.StatusUnauthorized, c.Error(errors.New("invalid Authorization header format")))
			c.Abort()

			return
		}

		token, _, err := new(jwt.Parser).ParseUnverified(tokenString, jwt.MapClaims{})
		if err != nil {
			c.JSON(http.StatusUnauthorized, c.Error(errors.New("invalid token")))
			c.Abort()

			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			c.JSON(http.StatusUnauthorized, c.Error(errors.New("unable to extract claims")))
			c.Abort()

			return
		}

		userID, ok := claims["sub"].(string)
		if !ok {
			c.JSON(http.StatusUnauthorized, c.Error(errors.New("user ID not found in token")))
			c.Abort()

			return
		}

		c.Set("userID", userID)
		c.Next()
	}
}
