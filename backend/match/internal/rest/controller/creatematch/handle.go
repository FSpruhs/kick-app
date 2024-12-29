package creatematch

import (
	"fmt"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	"github.com/FSpruhs/kick-app/backend/match/internal/application"
	"github.com/FSpruhs/kick-app/backend/match/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/match/internal/domain"
)

// Handle
// CreateMatch godoc
// @Summary      creates new match
// @Description  creates new match
// @Tags         match
// @Accept       json
// @Produce      json
// @Success      201  {object}  Response
// @Failure      400
// @Failure      500
// @Router       /match [post].
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

		command, err := toCommand(&message)
		if err != nil {
			context.JSON(http.StatusBadRequest, context.Error(err))

			return
		}

		result, err := app.CreateMatch(command)
		if err != nil {
			context.JSON(http.StatusInternalServerError, context.Error(err))

			return
		}

		context.JSON(http.StatusCreated, toMessage(result))
	}
}

func toCommand(message *Message) (*commands.CreateMatch, error) {
	dateTime, err := time.Parse(time.RFC3339, message.Begin)
	if err != nil {
		return nil, fmt.Errorf("parse date time: %w", err)
	}

	location, err := domain.NewLocation(message.Location)
	if err != nil {
		return nil, fmt.Errorf("create location: %w", err)
	}

	playerCount, err := domain.NewPlayerCount(message.MinPlayers, message.MaxPlayers)
	if err != nil {
		return nil, fmt.Errorf("create player count: %w", err)
	}

	return &commands.CreateMatch{
		UserID:      message.UserID,
		GroupID:     message.GroupID,
		Begin:       dateTime,
		Location:    location,
		PlayerCount: playerCount,
	}, nil
}

func toMessage(match *domain.Match) *Response {
	return &Response{
		ID: match.ID(),
	}
}
