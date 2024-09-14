package inviteduserresponse

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

// Handle
// InvitedUserResponse godoc
// @Summary      handels invited user response
// @Description  handels if a user accepts or declines an invite to a group
// @Tags         group
// @Accepted       json
// @Produce      json
// @Success      200
// @Failure      400
// @Router       /group/user [put].
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

		command := commands.InvitedUserResponse{
			UserID:   message.UserID,
			GroupID:  message.GroupID,
			Accepted: message.Accepted,
		}

		if err := app.InvitedUserResponse(&command); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, nil)
	}
}
