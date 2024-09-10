package domain

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

const PlayerAggregate = "player.PlayerAggregate"

var (
	ErrDifferentGroups            = errors.New("players are not in the same group")
	ErrInsufficientPermissions    = errors.New("updating player has to be at least an admin")
	ErrSelfUpdate                 = errors.New("player cannot update their own role")
	ErrMasterDowngrade            = errors.New("only master can downgrade to member")
	ErrMasterUpdate               = errors.New("only master can update to master")
	ErrMasterDowngradeByNonMaster = errors.New("only master can downgrade a master")
)

type Player struct {
	ddd.Aggregate
	GroupID string
	UserID  string
	Role    PlayerRole
}

func (p *Player) UpdateRole(updatingPlayer *Player, newRole PlayerRole) error {
	if err := validateUpdateRolePermission(updatingPlayer, p, newRole); err != nil {
		return fmt.Errorf("validating update role permission: %w", err)
	}

	p.Role = newRole

	if newRole != Master {
		return nil
	}

	if err := updatingPlayer.UpdateRole(p, Admin); err != nil {
		return fmt.Errorf("updating role of updating player: %w", err)
	}

	return nil
}

func validateUpdateRolePermission(updatingPlayer, targetPlayer *Player, newRole PlayerRole) error {
	if targetPlayer.GroupID != updatingPlayer.GroupID {
		return ErrDifferentGroups
	}

	if updatingPlayer.Role == Member {
		return ErrInsufficientPermissions
	}

	if updatingPlayer.ID() == targetPlayer.ID() {
		return ErrSelfUpdate
	}

	switch newRole {
	case Member:
		if updatingPlayer.Role != Master {
			return ErrMasterDowngrade

		}
	case Master:
		if updatingPlayer.Role != Master {
			return ErrMasterUpdate
		}
	case Admin:
		if targetPlayer.Role == Master && updatingPlayer.Role != Master {
			return ErrMasterDowngradeByNonMaster
		}
	}

	return nil
}
