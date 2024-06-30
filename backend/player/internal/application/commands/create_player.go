package commands

import "github.com/FSpruhs/kick-app/backend/player/internal/domain"

type CreatePlayer struct {
	FirstName string
	LastName  string
}

type CreatePlayerHandler struct {
	domain.PlayerRepository
}

func NewCreatePlayerHandler(players domain.PlayerRepository) CreatePlayerHandler {
	return CreatePlayerHandler{players}
}

func (h CreatePlayerHandler) CreatePlayer(cmd CreatePlayer) (*domain.Player, error) {
	newPlayer := domain.Player{
		FirstName: cmd.FirstName,
		LastName:  cmd.LastName,
	}
	result, err := h.PlayerRepository.Create(&newPlayer)
	if err != nil {
		return nil, err
	}
	return result, nil
}
