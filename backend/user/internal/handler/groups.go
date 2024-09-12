package handler

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

func RegisterGroupHandler(
	groupHandler ddd.EventHandler[ddd.AggregateEvent],
	domainSubscriber ddd.EventSubscriber[ddd.AggregateEvent],
) {
	domainSubscriber.Subscribe(grouppb.UserInvitedEvent, groupHandler)
	domainSubscriber.Subscribe(grouppb.UserAcceptedInvitationEvent, groupHandler)
	domainSubscriber.Subscribe(grouppb.GroupCreatedEvent, groupHandler)
}
