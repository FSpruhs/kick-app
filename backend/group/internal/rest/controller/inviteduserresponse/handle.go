package inviteduserresponse

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var message Message

		if err := c.BindJSON(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		if validationErr := validator.New().Struct(&message); validationErr != nil {
			c.JSON(http.StatusBadRequest, c.Error(validationErr))

			return
		}

		command := commands.InvitedUserResponse{
			UserID:  message.UserID,
			GroupID: message.GroupID,
			Accept:  message.Accept,
		}

		if err := app.InvitedUserResponse(&command); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		c.JSON(http.StatusOK, nil)
	}
}
