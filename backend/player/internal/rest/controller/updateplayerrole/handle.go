package updateplayerrole

import (
	"errors"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
)

// UpdatePlayerRole godoc
// @Summary      updates role of a player
// @Description  updates role of a player
// @Accept       json
// @Produce      json
// @Success      200  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Failure      500  {object}  httputil.HTTPError
// @Router       /player/role [put]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var message Message

		if err := c.BindJSON(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		if err := validator.New().Struct(&message); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		command, err := toCommand(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		err = app.UpdateRole(command)
		if errors.Is(err, domain.InvalidPlayerRoleError{}) {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		} else if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))

			return
		}

		c.JSON(http.StatusOK, nil)
	}
}

func toCommand(message *Message) (*commands.UpdateRole, error) {
	role, err := domain.ToPlayerRole(message.NewRole)
	if err != nil {
		return nil, fmt.Errorf("mapping message to command: %w", err)
	}

	return &commands.UpdateRole{
		PlayerToUpdateID: message.PlayerToUpdateID,
		UpdatingPlayerID: message.UpdatingPlayerID,
		NewRole:          role,
	}, nil
}
