package inviteuser

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

var validate = validator.New()

func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		var message Message

		if err := context.BindJSON(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if validationErr := validate.Struct(&message); validationErr != nil {
			context.JSON(http.StatusBadRequest, context.Error(validationErr))

			return
		}

		inviteUserCommand := commands.InviteUser{
			UserID:  message.UserID,
			GroupID: message.GroupID,
			PayerID: message.PlayerID,
		}

		if err := app.InviteUser(&inviteUserCommand); err != nil {
			switch {
			case errors.Is(err, domain.ErrGroupNotFound):
				context.JSON(http.StatusNotFound, context.Error(err))

				return
			default:
				context.JSON(http.StatusInternalServerError, context.Error(err))

				return
			}
		}

		context.JSON(http.StatusCreated, nil)
	}
}
