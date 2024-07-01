package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNewFullName(t *testing.T) {
	tests := []struct {
		firstName string
		lastName  string
		expectErr bool
	}{
		{"John", "Doe", false},
		{"", "Doe", true},
		{"John", "", true},
		{"JohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohn", "Doe", true},
	}

	for _, tt := range tests {
		_, err := NewFullName(tt.firstName, tt.lastName)
		if tt.expectErr {
			assert.Error(t, err, "NewFullName(%s, %s): expected error, but got none", tt.firstName, tt.lastName)
		} else {
			assert.NoError(t, err, "NewFullName(%s, %s): expected no error, but got %v", tt.firstName, tt.lastName, err)
		}
	}
}

func TestFullNameGetter(t *testing.T) {
	fullName, err := NewFullName("John", "Doe")
	assert.NoError(t, err, "Error creating FullName object: %v", err)

	assert.Equal(t, "John", fullName.FirstName(), "Expected first name 'John', but got '%s'", fullName.FirstName())
	assert.Equal(t, "Doe", fullName.LastName(), "Expected last name 'Doe', but got '%s'", fullName.LastName())
}
