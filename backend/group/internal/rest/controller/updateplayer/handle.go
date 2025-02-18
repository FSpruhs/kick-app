package updateplayer

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

// Handle
// UpdatePlayer godoc
// @Summary      updates player of a group
// @Description  updates player of a group
// @Tags         group
// @Accept       json
// @Produce      json
// @Success      200
// @Failure      400
// @Failure      500
// @Router       /group/player [put].
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

		role, err := domain.ToRole(message.Role)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		status, err := domain.ToStatus(message.Status)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		command := commands.UpdatePlayer{
			GroupID:        message.GroupID,
			UpdatingUserID: message.UpdatingUserID,
			UpdatedUserID:  message.UpdatedUserID,
			NewRole:        role,
			NewStatus:      status,
		}

		if err := app.UpdatePlayer(&command); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
