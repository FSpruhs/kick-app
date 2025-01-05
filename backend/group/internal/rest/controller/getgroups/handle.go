package getgroups

import (
	"errors"
	"log"
	"net/http"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/gin-gonic/gin"
)

// Handle
// GetGroupsByUser godoc
// @Summary      get groups by user id
// @Description  get groups by user id
// @Tags         group
// @Accepted       json
// @Produce      json
// @Success      200  {array}  	Response
// @Failure      400
// @Router       /group/user/{userId} [get].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		userID, exists := context.Get("userID")
		if !exists {
			context.JSON(http.StatusBadRequest, context.Error(errors.New("user ID not found in context")))

			return
		}

		userIDStr, ok := userID.(string)
		if !ok {
			context.JSON(http.StatusInternalServerError, gin.H{"error": "User ID is not a string"})

			return
		}

		log.Printf("User ID: %s", userIDStr)

		command := &queries.GetGroupsByUser{
			UserID: userIDStr,
		}

		groups, err := app.GetGroups(command)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, toResponse(groups))
	}
}

func toResponse(groups []*domain.Group) []*Response {
	var response = make([]*Response, len(groups))

	for i, group := range groups {
		response[i] = &Response{
			ID:   group.ID(),
			Name: group.Name().Value(),
		}
	}

	return response
}
