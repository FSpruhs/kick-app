package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNewPassword(t *testing.T) {
	tests := []struct {
		password string
		valid    bool
	}{
		{"Abcd123", true},
		{"abcdef", false},
		{"ABCDEF", false},
		{"123456", false},
		{"abc123", false},
		{"ABC123", false},
		{"Abcdef", false},
		{"Abcdefghi12345678", true},
		{"", false},
		{"Ab1", false},
	}

	for _, tt := range tests {
		t.Run(tt.password, func(t *testing.T) {
			_, err := NewPassword(tt.password)

			if tt.valid {
				assert.NoError(t, err, "Expected valid password '%s' to be accepted, but got error: %v", tt.password, err)
			} else {
				assert.Error(t, err, "Expected invalid password '%s' to return error, but got nil", tt.password)
			}
		})
	}
}

func TestPasswordGetter(t *testing.T) {
	passwordStr := "Abcd123"
	password, err := NewPassword(passwordStr)
	assert.NoError(t, err, "Error creating Password object: %v", err)

	assert.Equal(t, passwordStr, password.Clear(), "Expected clear password '%s', got '%s'", passwordStr, password.Clear())

	hash := password.Hash()
	assert.NotEmpty(t, hash, "Expected hashed password to be non-empty, but got empty string")
}

func TestNewHashedPassword(t *testing.T) {
	hash := "f27d5c571498ee9f3a8a8f3047bf0c88"
	password := NewHashedPassword(hash)

	assert.Equal(t, hash, password.Hash(), "Expected hashed password '%s', got '%s'", hash, password.Hash())
	assert.Empty(t, password.Clear(), "Expected clear password to be empty for hashed password, but got non-empty")
}
