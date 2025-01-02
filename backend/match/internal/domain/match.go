package domain

import (
	"errors"
	"fmt"
	"time"

	"github.com/google/uuid"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/matchpb"
)

const MatchAggregate = "match.MatchAggregate"

var ErrMatchAlreadyStarted = errors.New("match already started")

type Match struct {
	ddd.Aggregate
	groupID       string
	begin         time.Time
	location      Location
	playerCount   PlayerCount
	registrations []*Registration
}

func NewMatch(
	id,
	groupID string,
	begin time.Time,
	location *Location,
	playerCount *PlayerCount,
	registrations []*Registration,
) *Match {
	return &Match{
		Aggregate:     ddd.NewAggregate(id, MatchAggregate),
		groupID:       groupID,
		begin:         begin,
		location:      location,
		playerCount:   playerCount,
		registrations: registrations,
	}

}

func CreateNewMatch(begin time.Time, location Location, playerCount PlayerCount, groupID string) (*Match, error) {
	match := &Match{
		Aggregate:     ddd.NewAggregate(uuid.New().String(), MatchAggregate),
		groupID:       groupID,
		begin:         begin,
		location:      location,
		playerCount:   playerCount,
		registrations: make([]*Registration, 0),
	}

	if time.Now().After(begin) {
		return nil, ErrMatchAlreadyStarted
	}

	match.AddEvent(matchpb.MatchCreatedEvent, matchpb.MatchCreated{
		MatchID: match.ID(),
		GroupID: match.GroupID(),
	})

	return match, nil
}

func (m *Match) RespondToInvitation(playerID string, accept bool) error {
	var status RegistrationStatus
	if accept {
		status = Registered
	} else {
		status = Deregistered
	}

	for _, r := range m.registrations {
		if r.userID == playerID {
			if r.status != Registered && r.status != Deregistered {
				return fmt.Errorf("player %s cant change registration", playerID)
			}
			r.status = status
			r.timeStamp = time.Now()

			return nil
		}
	}

	m.registrations = append(m.registrations, &Registration{
		userID:    playerID,
		status:    status,
		timeStamp: time.Now(),
	})

	return nil
}

func (m *Match) AddRegistration(playerID string) error {
	for _, r := range m.registrations {
		if r.userID == playerID {
			r.status = Added

			return nil
		}
	}

	return fmt.Errorf("player %s not found", playerID)
}

func (m *Match) Begin() time.Time {
	return m.begin
}

func (m *Match) Location() Location {
	return m.location
}

func (m *Match) PlayerCount() PlayerCount {
	return m.playerCount
}

func (m *Match) Registrations() []*Registration {
	return m.registrations
}

func (m *Match) GroupID() string {
	return m.groupID
}
