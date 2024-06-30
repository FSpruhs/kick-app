package domain

import (
	"fmt"
	"net/mail"
)

var ErrEmailInvalid = fmt.Errorf("email is invalid")

type Email struct {
	value string
}

func NewEmail(value string) (*Email, error) {
	if !isEmailValid(value) {
		return nil, ErrEmailInvalid
	}
	return &Email{value: value}, nil
}

func (e Email) Value() string {
	return e.value
}

func isEmailValid(email string) bool {
	_, err := mail.ParseAddress(email)
	return err == nil
}
