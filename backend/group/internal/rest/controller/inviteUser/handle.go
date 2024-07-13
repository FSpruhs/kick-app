package inviteUser

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"net/http"
)

var validate = validator.New()

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {
		var message Message

		if err := c.BindJSON(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))
			return
		}

		if validationErr := validate.Struct(&message); validationErr != nil {
			c.JSON(http.StatusBadRequest, c.Error(validationErr))
			return
		}

		inviteUserCommand := commands.InviteUser{
			UserId:  message.UserId,
			GroupId: message.GroupId,
			PayerId: message.PlayerId,
		}

		if err := app.InviteUser(&inviteUserCommand); err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))
			return
		}

		c.JSON(http.StatusCreated, nil)
	}
}
