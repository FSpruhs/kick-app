package getgroupdetails

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/gin-gonic/gin"
)

// GetGroupDetails godoc
// @Summary      get group details by group id
// @Description  get group details by group id
// @Tags         group
// @Accepted     json
// @Produce      json
// @Success      200  {object}  	Response
// @Failure      400
// @Router       /group/{groupId} [get]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}
