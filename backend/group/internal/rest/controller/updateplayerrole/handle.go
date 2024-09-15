package updateplayerrole

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"net/http"
)

// Handle
// UpdatePlayerRole godoc
// @Summary      updates role of a player
// @Description  updates role of a player
// @Tags         group
// @Accept       json
// @Produce      json
// @Success      200
// @Failure      400
// @Failure      500
// @Router       /group/player/role [put].
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

		role, err := domain.ToRole(message.NewRole)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		command := commands.UpdatePlayerRole{
			GroupID:        message.GroupID,
			UpdatingUserID: message.UpdatingUserID,
			UpdatedUserID:  message.UpdatedUserID,
			NewRole:        role,
		}

		if err := app.UpdatePlayerRole(&command); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
