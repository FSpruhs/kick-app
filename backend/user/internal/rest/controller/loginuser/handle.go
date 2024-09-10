package loginuser

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

// LoginUser godoc
// @Summary      logs in user
// @Description  logs in user
// @Accept       json
// @Produce      json
// @Success      200  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Failure      500  {object}  httputil.HTTPError
// @Router       /user/login [post]
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

		email, err := domain.NewEmail(message.Email)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		password, err := domain.NewPassword(message.Password)
		if err != nil {
			c.JSON(http.StatusBadRequest, c.Error(err))

			return
		}

		loginUserCommand := commands.LoginUser{
			Email:    email,
			Password: password,
		}

		result, err := app.LoginUser(&loginUserCommand)
		if err != nil && errors.Is(err, domain.ErrWrongPassword) {
			c.JSON(http.StatusUnauthorized, c.Error(err))

			return
		} else if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))

			return
		}

		response := toResponse(result)

		c.JSON(http.StatusOK, response)
	}
}

func toResponse(result *domain.User) *Response {
	return &Response{
		ID:        result.ID,
		FirstName: result.FullName.FirstName(),
		LastName:  result.FullName.LastName(),
		Email:     result.Email.Value(),
		NickName:  result.NickName,
		Groups:    result.Groups,
	}
}
