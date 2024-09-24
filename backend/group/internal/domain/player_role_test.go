package domain

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestRole_ToRole(t *testing.T) {
	tests := []struct {
		roleString   string
		expectedRole Role
		expectedErr  error
	}{
		{"member", Member, nil},
		{"mEmBer", Member, nil},
		{"admin", Admin, nil},
		{"adMiN", Admin, nil},
		{"master", Master, nil},
		{"MasteR", Master, nil},
		{"unknown", -1, InvalidPlayerRoleError{"unknown"}},
	}
	for _, test := range tests {
		t.Run(fmt.Sprintf("player role: %s", test.expectedRole), func(t *testing.T) {
			role, err := ToRole(test.roleString)
			if test.expectedErr == nil {
				assert.NoError(t, err)
				assert.Equal(t, test.expectedRole, role)
			} else {
				assert.Error(t, err)
				assert.Equal(t, test.expectedErr, err)
			}
		})
	}
}
