package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestUserLogin(t *testing.T) {
	fullName, _ := NewFullName("John", "Doe")
	email, _ := NewEmail("john.doe@example.com")
	password, _ := NewPassword("Abcd123")
	user := NewUser(fullName, "johndoe", password, email)

	tests := []struct {
		name           string
		loginEmail     *Email
		loginPassword  *Password
		expectedError  error
		expectedUserID string
	}{
		{
			name:           "Successful login",
			loginEmail:     email,
			loginPassword:  password,
			expectedError:  nil,
			expectedUserID: user.ID,
		},
		{
			name:           "Wrong password",
			loginEmail:     email,
			loginPassword:  &Password{clear: "wrongpassword"},
			expectedError:  ErrWrongPassword,
			expectedUserID: "",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := user.Login(tt.loginEmail, tt.loginPassword)
			require.ErrorIs(t, err, tt.expectedError, "Expected error '%v', got '%v'", tt.expectedError, err)

			if err == nil {
				assert.Equal(t, tt.expectedUserID, user.ID, "Expected user ID '%s', got '%s'", tt.expectedUserID, user.ID)
			}
		})
	}
}
