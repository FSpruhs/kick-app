package domain

import (
	"testing"

	"github.com/stretchr/testify/require"

	"github.com/FSpruhs/kick-app/backend/internal/ddd"
)

func TestPlayer_UpdateRole(t *testing.T) {
	tests := []struct {
		name                 string
		updatingPlayer       *Player
		targetPlayer         *Player
		newRole              PlayerRole
		expectedError        error
		expectedTargetRole   PlayerRole
		expectedUpdatingRole PlayerRole
	}{
		{
			name:                 "updating member to admin from master",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Member},
			newRole:              Admin,
			expectedError:        nil,
			expectedTargetRole:   Admin,
			expectedUpdatingRole: Master,
		},
		{
			name:                 "updating admin to member from master",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Admin},
			newRole:              Member,
			expectedError:        nil,
			expectedTargetRole:   Member,
			expectedUpdatingRole: Master,
		},
		{
			name:                 "updating member to admin from admin",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Admin},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Member},
			newRole:              Admin,
			expectedError:        nil,
			expectedTargetRole:   Admin,
			expectedUpdatingRole: Admin,
		},
		{
			name:                 "updating member to master from master",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Member},
			newRole:              Master,
			expectedError:        nil,
			expectedTargetRole:   Master,
			expectedUpdatingRole: Admin,
		},
		{
			name:                 "updating admin to master from master",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Admin},
			newRole:              Master,
			expectedError:        nil,
			expectedTargetRole:   Master,
			expectedUpdatingRole: Admin,
		},
		{
			name:                 "self updating",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Member},
			newRole:              Admin,
			expectedError:        ErrSelfUpdate,
			expectedTargetRole:   Admin,
			expectedUpdatingRole: Master,
		},
		{
			name:                 "updating different group",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), GroupID: "1", Role: Master},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), GroupID: "2", Role: Member},
			newRole:              Admin,
			expectedError:        ErrDifferentGroups,
			expectedTargetRole:   Admin,
			expectedUpdatingRole: Master,
		},
		{
			name:                 "updating admin to member from admin",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Admin},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Admin},
			newRole:              Member,
			expectedError:        ErrMasterDowngrade,
			expectedTargetRole:   Member,
			expectedUpdatingRole: Admin,
		},
		{
			name:                 "updating admin to member from admin",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Admin},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("2", ""), Role: Admin},
			newRole:              Member,
			expectedError:        ErrMasterDowngrade,
			expectedTargetRole:   Member,
			expectedUpdatingRole: Admin,
		},
		{
			name:                 "updating admin to member from member",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Member},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Admin},
			newRole:              Member,
			expectedError:        ErrInsufficientPermissions,
			expectedTargetRole:   Member,
			expectedUpdatingRole: Member,
		},
		{
			name:                 "updating member to admin from member",
			updatingPlayer:       &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Member},
			targetPlayer:         &Player{Aggregate: ddd.NewAggregate("1", ""), Role: Member},
			newRole:              Admin,
			expectedError:        ErrInsufficientPermissions,
			expectedTargetRole:   Admin,
			expectedUpdatingRole: Member,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := tt.targetPlayer.UpdateRole(tt.updatingPlayer, tt.newRole)
			require.ErrorIs(t, err, tt.expectedError, "Expected error '%v', got '%v'", tt.expectedError, err)

			if err == nil {
				require.Equal(t, tt.expectedTargetRole, tt.targetPlayer.Role, "Expected target player role '%s', got '%s'", tt.expectedTargetRole, tt.targetPlayer.Role)
				require.Equal(t, tt.expectedUpdatingRole, tt.updatingPlayer.Role, "Expected updating player role '%s', got '%s'", tt.expectedUpdatingRole, tt.updatingPlayer.Role)
			}

		})
	}
}
