package domain

import (
	"errors"
	"regexp"
)

type Password struct {
	clear string
	hash  string
}

var ErrInvalidPassword = errors.New("invalid password")

func NewPassword(clear string) (*Password, error) {
	if !isPasswordValid(clear) {
		return nil, ErrInvalidPassword
	}

	return &Password{clear: clear, hash: hashString(clear)}, nil
}

func NewHashedPassword(hash string) *Password {
	return &Password{hash: hash}
}

func (p Password) Clear() string {
	return p.clear
}

func (p Password) Hash() string {
	return p.hash
}

func isPasswordValid(password string) bool {
	const minPasswordLength = 6
	if len(password) < minPasswordLength {
		return false
	}

	hasDigit := regexp.MustCompile(`[0-9]`).MatchString(password)
	hasUpper := regexp.MustCompile(`[A-Z]`).MatchString(password)
	hasLower := regexp.MustCompile(`[a-z]`).MatchString(password)

	return hasDigit && hasUpper && hasLower
}
