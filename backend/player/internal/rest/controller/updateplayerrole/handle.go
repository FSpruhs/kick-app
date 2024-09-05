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

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var updatePlayerRoleMessage Message

		if err := c.BindJSON(&updatePlayerRoleMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		}

		if err := validator.New().Struct(&updatePlayerRoleMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		}

		command, err := toCommand(updatePlayerRoleMessage)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		}

		err = app.UpdateRole(command)
		if errors.Is(err, domain.ErrUpdatingRole) {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})

			return
		} else if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})

			return
		}

		c.JSON(http.StatusOK, "")
	}
}

func toCommand(message Message) (*commands.UpdateRole, error) {
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
