package getuserall

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

// Handle
// GetUserAll godoc
// @Summary      gets all users
// @Description  gets all users
// @Tags         user
// @Accept       json
// @Produce      json
// @Success      200  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /user [get].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		filter := &domain.Filter{
			ExceptGroupID: context.Query("exceptGroupID"),
		}

		command := &queries.GetUserAll{Filter: filter}

		users, err := app.GetUserAll(command)
		if err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusOK, toResponse(users))
	}
}

func toResponse(users []*domain.User) []*Response {
	response := make([]*Response, len(users))
	for index, user := range users {
		response[index] = &Response{
			ID:       user.ID,
			Email:    user.Email.Value(),
			NickName: user.NickName,
		}
	}

	return response
}
