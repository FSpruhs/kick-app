package domain

import (
	"errors"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
	"github.com/FSpruhs/kick-app/backend/player/playerspb"
)

const PlayerAggregate = "player.PlayerAggregate"

var (
	ErrDifferentGroups    = errors.New("players are not in the same group")
	ErrNotAdmin           = errors.New("updating player has to be at least an admin")
	ErrSelfUpdate         = errors.New("player cannot update their own role")
	ErrOnlyMasterToMember = errors.New("only master can update to member")
	ErrOnlyMasterToMaster = errors.New("only master can update to master")
)

type Player struct {
	ddd.Aggregate
	GroupID string
	UserID  string
	Role    PlayerRole
}

func (p *Player) UpdateRole(updatingPlayer *Player, newRole PlayerRole) error {
	if p.GroupID != updatingPlayer.GroupID {
		return ErrDifferentGroups
	}

	if updatingPlayer.Role == Member {
		return ErrNotAdmin
	}

	if p.ID() == updatingPlayer.ID() {
		return ErrSelfUpdate
	}

	if newRole == Member && updatingPlayer.Role != Master {
		return ErrOnlyMasterToMember
	}

	if newRole == Master && updatingPlayer.Role != Master {
		return ErrOnlyMasterToMaster
	}

	p.Role = newRole

	if newRole == Master {
		p.AddEvent(playerspb.NewMasterAppointedEvent, playerspb.NewMasterAppointed{
			OldMasterID: updatingPlayer.ID(),
			NewMasterID: p.ID(),
		})
	}

	return nil
}
