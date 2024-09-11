package getgroups

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

// GetGroups godoc
// @Summary      get groups by user id
// @Description  get groups by user id
// @Tags         group
// @Accepted       json
// @Produce      json
// @Success      200  {array}  	Response
// @Failure      400
// @Router       /group/user/{userId} [get]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		userID := c.Param("userId")

		command := &queries.GetGroups{
			UserID: userID,
		}

		groups, err := app.GetGroups(command)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		c.JSON(http.StatusOK, toResponse(groups))
	}
}

func toResponse(groups []*domain.Group) []*Response {
	var response = make([]*Response, len(groups))
	for i, group := range groups {
		response[i] = &Response{
			ID:   group.ID(),
			Name: group.Name.Value(),
		}
	}

	return response
}
