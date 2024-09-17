package getgroupdetails

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
)

// Handle
// GetGroupDetails godoc
// @Summary      get group details by group id
// @Description  get group details by group id
// @Tags         group
// @Accepted     json
// @Produce      json
// @Success      200  {object}  	Response
// @Failure      400
// @Router       /group/{groupId} [get].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		groupID := context.Param("groupId")

		command := &queries.GetGroup{GroupID: groupID}

		group, err := app.GetGroup(command)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		groupDetails := toGroupDetails(group)

		context.JSON(http.StatusOK, groupDetails)
	}
}

func toGroupDetails(group *domain.GroupDetails) *Response {
	users := make([]*User, len(group.Users()))
	for i, u := range group.Users() {
		users[i] = &User{
			ID:     u.ID(),
			Name:   u.Name(),
			Role:   u.Role(),
			Status: u.Status(),
		}
	}

	return &Response{
		ID:          group.ID(),
		Name:        group.Name(),
		InviteLevel: group.InviteLevel(),
		Users:       users,
	}
}
