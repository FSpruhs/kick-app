package messageread

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
)

// MessageRead godoc
// @Summary      reads a message
// @Description  reads a message
// @Accept       json
// @Produce      json
// @Success      200  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Router       /user/login [post]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {
		var message Message

		if err := c.BindJSON(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		if err := validator.New().Struct(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		command := commands.MessageRead{
			MessageID: message.MessageID,
			UserID:    message.UserID,
			Read:      message.Read,
		}

		if err := app.MessageRead(&command); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		c.JSON(http.StatusOK, nil)
	}
}
