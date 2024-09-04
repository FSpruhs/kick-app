package creategroup

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
		var groupMessage Message

		if err := context.BindJSON(&groupMessage); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if validationErr := validate.Struct(&groupMessage); validationErr != nil {
			context.JSON(http.StatusBadRequest, context.Error(validationErr))

			return
		}

		groupCommand := commands.CreateGroup{
			Name:   groupMessage.Name,
			UserID: groupMessage.UserID,
		}

		result, err := app.CreateGroup(&groupCommand)
		if err != nil {
			switch {
			case errors.Is(err, domain.ErrInvalidName):
				context.JSON(http.StatusBadRequest, context.Error(err))

				return
			default:
				context.JSON(http.StatusInternalServerError, context.Error(err))

				return
			}
		}

		context.JSON(http.StatusCreated, toResponse(result))
	}
}

func toResponse(group *domain.Group) *Response {
	return &Response{
		ID:   group.ID(),
		Name: group.Name.Value(),
	}
}
