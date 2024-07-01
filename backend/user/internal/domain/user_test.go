package domain

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
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
			expectedUserID: "",
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
			assert.True(t, errors.Is(err, tt.expectedError), "Expected error '%v', got '%v'", tt.expectedError, err)

			if err == nil {
				assert.Equal(t, tt.expectedUserID, user.Id, "Expected user ID '%s', got '%s'", tt.expectedUserID, user.Id)
			}
		})
	}
}

func TestUserSetId(t *testing.T) {
	user := &User{}

	user.SetId("12345")
	assert.Equal(t, "12345", user.Id, "Expected user ID '12345', got '%s'", user.Id)
}
