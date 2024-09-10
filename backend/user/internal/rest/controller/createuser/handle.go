package createuser

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

// CreateUser godoc
// @Summary      creates new user
// @Description  creates new user
// @Accept       json
// @Produce      json
// @Success      201  {object}  model.Account
// @Failure      400  {object}  httputil.HTTPError
// @Failure      500  {object}  httputil.HTTPError
// @Router       /user [post]
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

		user, err := app.CreateUser(command)
		if err != nil {
			c.JSON(http.StatusInternalServerError, c.Error(err))

			return
		}

		c.JSON(http.StatusCreated, toResponse(user))
	}
}

func toResponse(user *domain.User) *Response {
	return &Response{
		ID:        user.ID,
		FirstName: user.FullName.FirstName(),
		LastName:  user.FullName.LastName(),
		Email:     user.Email.Value(),
		NickName:  user.NickName,
		Groups:    user.Groups,
	}
}

func toCommand(message *Message) (*commands.CreateUser, error) {

	fullName, err := domain.NewFullName(message.FirstName, message.LastName)
	if err != nil {
		return nil, fmt.Errorf("creating full name: %w", err)
	}

	email, err := domain.NewEmail(message.Email)
	if err != nil {
		return nil, fmt.Errorf("creating email: %w", err)
	}

	password, err := domain.NewPassword(message.Password)
	if err != nil {
		return nil, domain.ErrInvalidPassword
	}

	return &commands.CreateUser{
		FullName: fullName,
		Nickname: message.NickName,
		Email:    email,
		Password: password,
	}, nil
}
