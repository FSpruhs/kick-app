package creategroup

import (
	"errors"
	"github.com/FSpruhs/kick-app/backend/group/internal/application"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"net/http"
)

var validate = validator.New()

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {
		var groupMessage Message

		if err := c.BindJSON(&groupMessage); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))
			return
		}

		if validationErr := validate.Struct(&groupMessage); validationErr != nil {
			c.JSON(http.StatusBadRequest, c.Error(validationErr))
			return
		}

		groupCommand := commands.CreateGroup{
			Name:   groupMessage.Name,
			UserID: groupMessage.UserId,
		}

		result, err := app.CreateGroup(&groupCommand)
		if err != nil {
			switch {
			case errors.Is(err, domain.ErrInvalidName):
				c.JSON(http.StatusBadRequest, c.Error(err))
				return
			default:
				c.JSON(http.StatusInternalServerError, c.Error(err))
				return
			}

		}

		c.JSON(http.StatusCreated, toResponse(result))
	}
}

func toResponse(group *domain.Group) *Response {
	return &Response{
		Id:   group.ID(),
		Name: group.Name.Value(),
	}
}
