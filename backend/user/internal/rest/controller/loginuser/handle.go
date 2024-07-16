package loginuser

import (
	"errors"
	"net/http"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var userMessage Message

		if err := c.BindJSON(&userMessage); err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		if validationErr := validate.Struct(&userMessage); validationErr != nil {
			c.JSON(http.StatusBadRequest, c.Error(validationErr))

			return
		}

		email, err := domain.NewEmail(userMessage.Email)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))
			return
		}

		password, err := domain.NewPassword(userMessage.Password)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))
			return
		}

		loginUserCommand := commands.LoginUser{
			Email:    email,
			Password: password,
		}

		result, err := app.LoginUser(loginUserCommand)
		if err != nil && errors.Is(err, domain.ErrWrongPassword) {
			c.JSON(http.StatusUnauthorized, c.Error(err))
			return
		} else if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))
			return
		}

		response := Response{
			ID:        result.Id,
			FirstName: result.FullName.FirstName(),
			LastName:  result.FullName.LastName(),
			Email:     result.Email.Value(),
			NickName:  result.NickName,
			Groups:    result.Groups,
		}

		c.JSON(http.StatusOK, response)
	}
}
