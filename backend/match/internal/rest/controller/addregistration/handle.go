package addregistration

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/application/commands"
)

// Handle
// AddRegistration godoc
// @Summary      adds registration to a match
// @Description  adds registration to a match
// @Tags         match
// @Accept       json
// @Produce      json
// @Success      201  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /match/registration [put].
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

		if err := app.AddRegistration(toCommand(&message)); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, nil)
	}
}

func toCommand(message *Message) *commands.AddRegistration {
	return &commands.AddRegistration{
		UserID:       message.UserID,
		MatchID:      message.MatchID,
		AddingUserID: message.AddingUserID,
	}
}
