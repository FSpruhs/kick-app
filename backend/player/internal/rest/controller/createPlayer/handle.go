package createPlayer

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
	"github.com/FSpruhs/kick-app/backend/player/internal/application/commands"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"net/http"
)

var validate = validator.New()

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var playerMessage Message

		if err := c.BindJSON(&playerMessage); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))
			return
		}

		if validationErr := validate.Struct(&playerMessage); validationErr != nil {
			c.JSON(http.StatusBadRequest, c.Error(validationErr))
			return
		}

		playerCommand := commands.CreatePlayer{
			FirstName: playerMessage.FirstName,
			LastName:  playerMessage.LastName,
		}

		result, err := app.CreatePlayer(playerCommand)
		if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))
			return
		}

		c.JSON(http.StatusCreated, result)
	}
}
