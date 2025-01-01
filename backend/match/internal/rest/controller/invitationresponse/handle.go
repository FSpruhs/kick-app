package invitationresponse

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/application/commands"
)

// Handle
// InvitationResponse godoc
// @Summary      response to match invitation
// @Description  response to match invitation
// @Tags         match
// @Accept       json
// @Produce      json
// @Success      201  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /match/registration [post].
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

		if err := app.RespondToInvitation(toCommand(&message)); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, nil)
	}
}

func toCommand(message *Message) *commands.RespondToInvitation {
	return &commands.RespondToInvitation{
		MatchID:            message.MatchID,
		Accept:             message.Accept,
		RespondingPlayerID: message.RespondingPlayerID,
		PlayerID:           message.PlayerID,
	}
}
