package handler

import (
	"github.com/FSpruhs/kick-app/backend/group/grouppb"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/internal/application"
)

func RegisterGroupHandler(groupHandler *application.GroupHandler, domainSubscriber ddd.EventSubscriber) {
	domainSubscriber.Subscribe(grouppb.GroupCreated{}, groupHandler.OnGroupCreated)
}
