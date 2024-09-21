package removeuser

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

// Handle InviteUser godoc
// @Summary      removes user from a group
// @Description  removes user from a group
// @Tags         group
// @Accepted     json
// @Produce      json
// @Success      200
// @Failure      400
// @Failure      500
// @Router       /group/user/status [put].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		var message Message

		if err := context.BindJSON(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if err := validator.New().Struct(message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		command := commands.RemovePlayer{
			GroupID:        message.GroupID,
			RemoveUserID:   message.RemoveUserID,
			RemovingUserID: message.RemovingUserID,
		}

		if err := app.RemovePlayer(&command); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
