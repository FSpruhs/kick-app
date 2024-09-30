package ginconfig

import (
	"context"
	"crypto/tls"
	"net/http"
	"strings"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"github.com/gin-gonic/gin"
)

func JWTValidator() gin.HandlerFunc {
	return func(c *gin.Context) {
		rawAccessToken := c.GetHeader("Authorization")
		token := strings.TrimPrefix(rawAccessToken, "Bearer ")

		tr := &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		}

		client := &http.Client{
			Timeout:   time.Duration(6000) * time.Second,
			Transport: tr,
		}

		ctx := oidc.ClientContext(context.Background(), client)

		provider, err := oidc.NewProvider(ctx, "http://localhost:8080/realms/kick-app")
		if err != nil {
			c.JSON(http.StatusUnauthorized, c.Error(err))
			c.Abort()

			return
		}

		oidcConfig := &oidc.Config{
			ClientID: "kick",
		}

		verifier := provider.Verifier(oidcConfig)

		_, err = verifier.Verify(ctx, token)
		if err != nil {
			c.JSON(http.StatusUnauthorized, c.Error(err))
			c.Abort()

			return
		}

		c.Next()
	}
}
