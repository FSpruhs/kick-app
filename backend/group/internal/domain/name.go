package domain

import (
	"errors"
)

var ErrInvalidName = errors.New("invalid name")

type Name struct {
	value string
}

func NewName(name string) (*Name, error) {
	if !isNameValid(name) {
		return nil, ErrInvalidName
	}

	return &Name{value: name}, nil
}

func isNameValid(name string) bool {
	return 40 > len(name) && len(name) > 0
}

func (n Name) Value() string {
	return n.value
}
