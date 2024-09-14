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

// Handle
// LoginUser godoc
// @Summary      logs in user
// @Description  logs in user
// @Tags         user
// @Accept       json
// @Produce      json
// @Success      200  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /message/read [post].
func Handle(app application.App) gin.HandlerFunc {
	return func(context *gin.Context) {
		var message Message

		if err := context.BindJSON(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		if err := validator.New().Struct(&message); err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		email, err := domain.NewEmail(message.Email)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		password, err := domain.NewPassword(message.Password)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		loginUserCommand := commands.LoginUser{
			Email:    email,
			Password: password,
		}

		result, err := app.LoginUser(&loginUserCommand)
		if err != nil && errors.Is(err, domain.ErrWrongPassword) {
			context.JSON(http.StatusUnauthorized, context.Error(err))

			return
		} else if err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		response := toResponse(result)

		context.JSON(http.StatusOK, response)
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
