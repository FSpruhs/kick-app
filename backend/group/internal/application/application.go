package application

import (
	"github.com/FSpruhs/kick-app/backend/group/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/group/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/group/internal/domain"
	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type App interface {
	Commands
	Queries
}

type Commands interface {
	CreateGroup(cmd *commands.CreateGroup) (*domain.Group, error)
	InviteUser(cmd *commands.InviteUser) error
	InvitedUserResponse(cmd *commands.InvitedUserResponse) error
	LeaveGroup(cmd *commands.LeaveGroup) error
}

type Queries interface {
	GetGroups(cmd *queries.GetGroups) ([]*domain.Group, error)
	GetGroup(cmd *queries.GetGroup) (*domain.GroupDetails, error)
}

type Application struct {
	appCommands
	appQueries
}

type appCommands struct {
	commands.CreateGroupHandler
	commands.InviteUserHandler
	commands.InvitedUserResponseHandler
	commands.LeaveGroupHandler
}

type appQueries struct {
	queries.GetGroupsHandler
	queries.GetGroupHandler
}

var _ App = (*Application)(nil)

func New(
	groups domain.GroupRepository,
	users domain.UserRepository,
	players domain.PlayerRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) *Application {
	return &Application{
		appCommands: appCommands{
			CreateGroupHandler:         commands.NewCreateGroupHandler(groups, eventPublisher),
			InviteUserHandler:          commands.NewInviteUserHandler(groups, eventPublisher, players),
			InvitedUserResponseHandler: commands.NewInvitedUserResponseHandler(groups, eventPublisher),
			LeaveGroupHandler:          commands.NewLeaveGroupHandler(groups, players),
		},
		appQueries: appQueries{
			GetGroupsHandler: queries.NewGetGroupsHandler(groups),
			GetGroupHandler:  queries.NewGetGroupHandler(groups, users),
		},
	}
}
