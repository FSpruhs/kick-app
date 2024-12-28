package domain

import (
	"time"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/google/uuid"
)

const MatchAggregate = "match.MatchAggregate"

type Match struct {
	ddd.Aggregate
	begin         time.Time
	location      Location
	playerCount   PlayerCount
	registrations []*Registration
}

func NewMatch(begin time.Time, location Location, playerCount PlayerCount) *Match {
	return &Match{
		Aggregate:     ddd.NewAggregate(uuid.New().String(), MatchAggregate),
		begin:         begin,
		location:      location,
		playerCount:   playerCount,
		registrations: make([]*Registration, 0),
	}
}

func (m Match) ID() string {
	return m.ID()
}

func (m Match) Begin() time.Time {
	return m.begin
}

func (m Match) Location() Location {
	return m.location
}

func (m Match) PlayerCount() PlayerCount {
	return m.playerCount
}

func (m Match) Registrations() []*Registration {
	return m.registrations
}
