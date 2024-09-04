package domain

import (
	"errors"
	"fmt"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

const PlayerAggregate = "player.PlayerAggregate"

var ErrUpdatingRole = errors.New("error updating role")

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
		return fmt.Errorf("error updating role: %w", err)
	}

	return nil
}

func validateUpdateRolePermission(updatingPlayer, targetPlayer *Player, newRole PlayerRole) error {
	if targetPlayer.GroupID != updatingPlayer.GroupID {
		return fmt.Errorf("players are not in the same group: %w", ErrUpdatingRole)
	}

	if updatingPlayer.Role == Member {
		return fmt.Errorf("updating player has to be at least an admin: %w", ErrUpdatingRole)
	}

	if updatingPlayer.ID() == targetPlayer.ID() {
		return fmt.Errorf("player cannot update their own role: %w", ErrUpdatingRole)
	}

	switch newRole {
	case Member:
		if updatingPlayer.Role != Master {
			return fmt.Errorf("only master can downgrade to member: %w", ErrUpdatingRole)
		}
	case Master:
		if updatingPlayer.Role != Master {
			return fmt.Errorf("only master can update to master: %w", ErrUpdatingRole)
		}
	case Admin:
		if targetPlayer.Role == Master && updatingPlayer.Role != Master {
			return fmt.Errorf("only master can downgrade a master: %w", ErrUpdatingRole)
		}
	}

	return nil
}
