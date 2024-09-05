package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNewEmail(t *testing.T) {
	tests := []struct {
		email    string
		valid    bool
		expected string
	}{
		{"test@example.com", true, "test@example.com"},
		{"invalid-email", false, ""},
		{"@missing-localpart.com", false, ""},
		{"missing@.com", false, ""},
		{"spaces@example.com ", true, "spaces@example.com"},
		{" test@example.com", true, "test@example.com"},
		{"te..st@example.com", false, ""},
		{"test.@example.com", false, ""},
		{"test@example..com", false, ""},
		{"test@.example.com", false, ""},
	}

	for _, tt := range tests {
		t.Run(tt.email, func(t *testing.T) {
			email, err := NewEmail(tt.email)

			if tt.valid {
				assert.NoError(t, err, "Expected valid email '%s' to be accepted, but got error: %v", tt.email, err)
				if email != nil {
					assert.Equal(t, tt.expected, email.Value(), "Expected email value '%s', got '%s'", tt.expected, email.Value())
				}
			} else {
				assert.Error(t, err, "Expected invalid email '%s' to return error, but got nil", tt.email)
			}
		})
	}
}
