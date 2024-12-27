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
	UpdatePlayer(cmd *commands.UpdatePlayer) error
	RemovePlayer(cmd *commands.RemovePlayer) error
}

type Queries interface {
	GetGroups(cmd *queries.GetGroupsByUser) ([]*domain.Group, error)
	GetGroup(cmd *queries.GetGroup) (*domain.GroupDetails, error)
	IsPlayerActive(cmd *queries.IsPlayerActive) bool
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
	commands.UpdatePlayerHandler
	commands.RemovePlayerHandler
}

type appQueries struct {
	queries.GetGroupsByUserHandler
	queries.GetGroupHandler
	queries.IsPlayerActiveHandler
}

var _ App = (*Application)(nil)

func New(
	groups domain.GroupRepository,
	users domain.UserRepository,
	eventPublisher ddd.EventPublisher[ddd.AggregateEvent],
) *Application {
	return &Application{
		appCommands: appCommands{
			CreateGroupHandler:         commands.NewCreateGroupHandler(groups, eventPublisher),
			InviteUserHandler:          commands.NewInviteUserHandler(groups, eventPublisher),
			InvitedUserResponseHandler: commands.NewInvitedUserResponseHandler(groups, eventPublisher),
			LeaveGroupHandler:          commands.NewLeaveGroupHandler(groups, eventPublisher),
			UpdatePlayerHandler:        commands.NewUpdatePlayerHandler(groups),
			RemovePlayerHandler:        commands.NewRemovePlayerHandler(groups, eventPublisher),
		},
		appQueries: appQueries{
			GetGroupsByUserHandler: queries.NewGetGroupsByUserHandler(groups),
			GetGroupHandler:        queries.NewGetGroupHandler(groups, users),
			IsPlayerActiveHandler:  queries.NewIsPlayerActiveHandler(groups),
		},
	}
}
