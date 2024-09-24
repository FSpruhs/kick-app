package domain

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"strings"
	"testing"
)

func TestNewName(t *testing.T) {
	tests := []struct {
		name string
		err  error
	}{
		{"name", nil},
		{"", ErrInvalidName},
		{strings.Repeat("a", 40), ErrInvalidName},
		{strings.Repeat("a", 39), nil},
	}

	for _, test := range tests {
		t.Run(fmt.Sprintf("Test Name: %s", test.name), func(t *testing.T) {
			name, err := NewName(test.name)
			if test.err == nil {
				assert.NoError(t, err)
				assert.Equal(t, test.name, name.Value())
			} else {
				assert.Error(t, err)
			}
		})

	}
}
