package domain

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestStatus_ToStatus(t *testing.T) {
	tests := []struct {
		statusString   string
		expectedStatus Status
		expectedErr    error
	}{
		{"active", Active, nil},
		{"aCtIve", Active, nil},
		{"inactive", Inactive, nil},
		{"inACtiVe", Inactive, nil},
		{"removed", Removed, nil},
		{"RemoVed", Removed, nil},
		{"leaved", Leaved, nil},
		{"leAVed", Leaved, nil},
		{"not_found", NotFound, nil},
		{"nOt_Found", NotFound, nil},
		{"unknown", -1, InvalidStatusError{"unknown"}},
	}
	for _, test := range tests {
		t.Run(fmt.Sprintf("player status: %s", test.expectedStatus), func(t *testing.T) {
			status, err := ToStatus(test.statusString)
			if test.expectedErr == nil {
				assert.NoError(t, err)
				assert.Equal(t, test.expectedStatus, status)
			} else {
				assert.Error(t, err)
				assert.Equal(t, test.expectedErr, err)
			}
		})
	}
}
