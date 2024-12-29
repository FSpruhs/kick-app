package domain

import (
	"time"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/matchpb"
)

const MatchAggregate = "match.MatchAggregate"

type Match struct {
	ddd.Aggregate
	groupID       string
	begin         time.Time
	location      Location
	playerCount   PlayerCount
	registrations []*Registration
}

func NewMatch(begin time.Time, location Location, playerCount PlayerCount, groupID string) *Match {
	match := &Match{
		Aggregate:     ddd.NewAggregate(uuid.New().String(), MatchAggregate),
		groupID:       groupID,
		begin:         begin,
		location:      location,
		playerCount:   playerCount,
		registrations: make([]*Registration, 0),
	}

	match.AddEvent(matchpb.MatchCreatedEvent, matchpb.MatchCreated{
		MatchID: match.ID(),
		GroupID: match.GroupID(),
	})

	return match
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

func (m Match) GroupID() string {
	return m.groupID
}
