package creategroup

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

// Handle
// CreateGroup godoc
// @Summary      creates a new Group
// @Description  user creates a new Group with a new name
// @Tags         group
// @Accepted       json
// @Produce      json
// @Success      201  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /group [post].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		var message Message

		if err := context.BindJSON(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if validationErr := validator.New().Struct(&message); validationErr != nil {
			context.JSON(http.StatusBadRequest, context.Error(validationErr))

			return
		}

		groupCommand := commands.CreateGroup{
			Name:   message.Name,
			UserID: message.UserID,
		}

		result, err := app.CreateGroup(&groupCommand)
		if err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, toResponse(result))
	}
}

func toResponse(group *domain.Group) *Response {
	return &Response{
		ID:   group.ID(),
		Name: group.Name().Value(),
	}
}
