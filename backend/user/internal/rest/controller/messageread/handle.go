package messageread

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
)

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var messageReadMessage Message

		if err := c.BindJSON(&messageReadMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		if err := validator.New().Struct(&messageReadMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		command := commands.MessageRead{
			MessageID: messageReadMessage.MessageID,
			UserID:    messageReadMessage.UserID,
			Read:      messageReadMessage.Read,
		}

		if err := app.MessageRead(&command); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		c.JSON(http.StatusOK, "")
	}
}
