package domain

import (
	"time"

	"github.com/google/uuid"
)

type Match struct {
	ID            string
	Begin         time.Time
	Location      Location
	PlayerCount   PlayerCount
	Registrations []*Registration
	Waiting       []*Registration
	Guests        []*Guest
}

func NewMatch(begin time.Time, location Location, playerCount PlayerCount) *Match {
	return &Match{
		ID:            uuid.New().String(),
		Begin:         begin,
		Location:      location,
		PlayerCount:   playerCount,
		Registrations: make([]*Registration, 0),
		Waiting:       make([]*Registration, 0),
		Guests:        make([]*Guest, 0),
	}
}
