package domain

import (
	"crypto/sha256"
	"fmt"
	"github.com/google/uuid"
)

var (
	ErrEmailAlreadyExists = fmt.Errorf("email already exists")
	ErrWrongPassword      = fmt.Errorf("wrong password")
)

type User struct {
	Id       string
	FullName *FullName
	NickName string
	Email    *Email
	Password *Password
	Groups   []string
}

func NewUser(fullName *FullName, nickName string, password *Password, email *Email) *User {
	return &User{
		Id:       uuid.New().String(),
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
		return fmt.Errorf("invalid email")
	}
	return nil
}

func hashString(input string) string {
	hash := sha256.New()
	hash.Write([]byte(input))
	return fmt.Sprintf("%x", hash.Sum(nil))
}
