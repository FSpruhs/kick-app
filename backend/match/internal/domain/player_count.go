package domain

import "errors"

type PlayerCount struct {
	min int
	max int
}

var ErrPlayerCountInvalid = errors.New("player count is invalid")

func NewPlayerCount(minPlayers, maxPlayers int) (*PlayerCount, error) {
	playerCount := PlayerCount{
		min: minPlayers,
		max: maxPlayers,
	}
	if !playerCount.isPlayerCountValid() {
		return nil, ErrPlayerCountInvalid
	}

	return &playerCount, nil
}

func (pc PlayerCount) isPlayerCountValid() bool {
	return pc.min > 0 && pc.min <= pc.max
}

func (pc PlayerCount) Min() int {
	return pc.min
}

func (pc PlayerCount) Max() int {
	return pc.max
}
