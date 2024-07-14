package inviteuser

import (
	"errors"
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
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
			UserID:  message.UserId,
			GroupID: message.GroupId,
			PayerID: message.PlayerId,
		}

		if err := app.InviteUser(&inviteUserCommand); err != nil {
			switch {
			case errors.Is(err, domain.ErrGroupNotFound):
				c.JSON(http.StatusNotFound, c.Error(err))
				return
			default:
				c.JSON(http.StatusInternalServerError, c.Error(err))
				return
			}
		}

		c.JSON(http.StatusCreated, nil)
	}
}
