package getusermessages

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

// Handle
// GetUserMessages godoc
// @Summary      gets all messages from a user
// @Description  gets all messages from a user
// @Tags         message
// @Accept       json
// @Produce      json
// @Success      200  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /message {userId} [get].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		userID := context.Param("userId")
		if userID == "" {
			context.JSON(http.StatusBadRequest, gin.H{"error": "missing userId"})

			return
		}

		command := &queries.GetUserMessages{UserID: userID}

		messages, err := app.GetUserMessages(command)
		if err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, toResponse(messages))
	}
}

func toResponse(messages []*domain.Message) []*Response {
	response := make([]*Response, len(messages))
	for index, message := range messages {
		response[index] = &Response{
			ID:         message.ID,
			UserID:     message.UserID,
			Content:    message.Content,
			Type:       message.Type.String(),
			OccurredAt: message.OccurredAt,
			Read:       message.Read,
		}
	}

	return response
}
