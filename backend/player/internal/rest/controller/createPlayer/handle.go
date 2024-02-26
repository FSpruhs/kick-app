package createPlayer

import (
	"github.com/FSpruhs/kick-app/backend/player/internal/domain"
	"github.com/FSpruhs/kick-app/backend/player/internal/mongodb"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"net/http"
)

var validate = validator.New()

func Handle(repository mongodb.PlayerRepository) gin.HandlerFunc {
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

		newPlayer := domain.Player{
			"",
			playerMessage.FirstName,
			playerMessage.LastName,
		}
		result, err := repository.Create(&newPlayer)
		if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))
			return
		}

		c.JSON(http.StatusCreated, result)
	}
}
