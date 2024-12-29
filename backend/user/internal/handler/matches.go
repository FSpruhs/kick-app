package handler

import (
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/match/matchpb"
)

func RegisterMatchHandler(
	matchHandler ddd.EventHandler[ddd.AggregateEvent],
	domainSubscriber ddd.EventSubscriber[ddd.AggregateEvent],
) {
	domainSubscriber.Subscribe(matchpb.MatchCreatedEvent, matchHandler)
}
