package leavegroup

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
)

// InviteUser godoc
// @Summary      user leaves a group
// @Description  user leaves a group
// @Accept       json
// @Produce      json
// @Success      201  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Failure      500  {object}  httputil.HTTPError
// @Router       /group/user [delte]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {
		var message Message

		if err := c.BindJSON(&message); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		}

		if err := validator.New().Struct(&message); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		}

		command := commands.LeaveGroup{
			GroupID: message.GroupID,
			UserID:  message.UserID,
		}

		if err := app.LeaveGroup(&command); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})

			return
		}

		c.JSON(http.StatusOK, nil)
	}
}
