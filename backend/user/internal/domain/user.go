package domain

import (
	"crypto/sha256"
	"errors"
	"fmt"

	"github.com/google/uuid"
)

var (
	ErrEmailAlreadyExists = errors.New("email already exists")
	ErrWrongPassword      = errors.New("wrong password")
	ErrInvalidEmail       = errors.New("invalid email")
)

type User struct {
	ID       string
	FullName *FullName
	NickName string
	Email    *Email
	Password *Password
	Groups   []string
}

func NewUser(fullName *FullName, nickName string, password *Password, email *Email) *User {
	return &User{
		ID:       uuid.New().String(),
		FullName: fullName,
		NickName: nickName,
		Email:    email,
		Password: password,
		Groups:   []string{},
	}
}

func (u *User) Login(email *Email, password *Password) error {
	if u.Password.Hash() != password.Hash() {
		return ErrWrongPassword
	}

	if u.Email.Value() != email.Value() {
		return ErrInvalidEmail
	}

	return nil
}

func hashString(input string) string {
	hash := sha256.New()
	hash.Write([]byte(input))

	return fmt.Sprintf("%x", hash.Sum(nil))
}
