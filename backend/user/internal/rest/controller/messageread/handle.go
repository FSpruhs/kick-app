package messageread

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
)

// Handle
// MessageRead godoc
// @Summary      reads a message
// @Description  reads a message
// @Tags         message
// @Accept       json
// @Produce      json
// @Success      200
// @Failure      400
// @Router       /user/login [put].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		var message Message

		if err := context.BindJSON(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if err := validator.New().Struct(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		command := commands.MessageRead{
			MessageID: message.MessageID,
			UserID:    message.UserID,
			Read:      message.Read,
		}

		if err := app.MessageRead(&command); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
