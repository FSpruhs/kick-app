package removeregistration

import (
	"net/http"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

// Handle
// RemoveRegistration godoc
// @Summary      removes registration from a match
// @Description  removes registration from a match
// @Tags         match
// @Accept       json
// @Produce      json
// @Success      200  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /match/registration [delete].
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

		if err := app.RemoveRegistration(toCommand(&message)); err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, nil)
	}
}
