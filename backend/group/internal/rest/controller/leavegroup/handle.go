package leavegroup

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

// Handle
// InviteUser godoc
// @Summary      user leaves a group
// @Description  user leaves a group
// @Tags         group
// @Accepted       json
// @Produce      json
// @Success      201
// @Failure      400
// @Failure      500
// @Router       /group/user [delete].
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

		command := commands.LeaveGroup{
			GroupID: message.GroupID,
			UserID:  message.UserID,
		}

		if err := app.LeaveGroup(&command); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
