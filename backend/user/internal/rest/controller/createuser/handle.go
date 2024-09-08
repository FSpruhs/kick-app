package createuser

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/user/internal/application"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

// @Summary ping example
// @Schemes
// @Description do ping
// @Tags example
// @Accept json
// @Produce json
// @Success 200 {string} Helloworld
// @Router /user [post]
func Handle(app application.App) gin.HandlerFunc {
	return func(c *gin.Context) {

		var userMessage Message

		if err := c.BindJSON(&userMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		if err := validator.New().Struct(&userMessage); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		command, err := toCommand(userMessage)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		user, err := app.CreateUser(command)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		c.JSON(http.StatusCreated, toResponse(user))
	}
}

func toResponse(user *domain.User) Response {
	return Response{
		ID:        user.Id,
		FirstName: user.FullName.FirstName(),
		LastName:  user.FullName.LastName(),
		Email:     user.Email.Value(),
		NickName:  user.NickName,
		Groups:    user.Groups,
	}
}

func toCommand(message Message) (*commands.CreateUser, error) {

	fullName, err := domain.NewFullName(message.FirstName, message.LastName)
	if err != nil {
		return nil, domain.ErrInvalidFullName
	}

	email, err := domain.NewEmail(message.Email)
	if err != nil {
		return nil, domain.ErrEmailInvalid
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
