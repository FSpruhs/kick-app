package domain

import (
	"errors"
	"net/mail"
	"strings"
)

var ErrEmailInvalid = errors.New("email is invalid")

type Email struct {
	value string
}

func NewEmail(value string) (*Email, error) {
	if !isEmailValid(value) {
		return nil, ErrEmailInvalid
	}

	return &Email{value: strings.TrimSpace(value)}, nil
}

func (e Email) Value() string {
	return e.value
}

func isEmailValid(email string) bool {
	_, err := mail.ParseAddress(email)

	return err == nil
}
