package leavegroup

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

var ErrRequiredGroupIDAndUserID = errors.New("groupId and userId are required")

// Handle InviteUser godoc
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
		groupID := context.Param("groupId")
		userID := context.Param("userId")

		if groupID == "" || userID == "" {
			context.JSON(http.StatusBadRequest, context.Error(ErrRequiredGroupIDAndUserID))

			return
		}

		command := commands.LeaveGroup{
			GroupID: groupID,
			UserID:  userID,
		}

		if err := app.LeaveGroup(&command); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
