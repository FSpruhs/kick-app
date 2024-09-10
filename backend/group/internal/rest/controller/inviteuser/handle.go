package inviteuser

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

// InviteUser godoc
// @Summary      invite a user to a group
// @Description  invite a user to a group
// @Accepted       json
// @Produce      json
// @Success      201  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Failure      500  {object}  httputil.HTTPError
// @Router       /group [post]
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

		inviteUserCommand := commands.InviteUser{
			UserID:  message.UserID,
			GroupID: message.GroupID,
			PayerID: message.PlayerID,
		}

		if err := app.InviteUser(&inviteUserCommand); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, nil)
	}
}
