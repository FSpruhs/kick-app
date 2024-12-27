package domain

import "errors"

type Location struct {
	name string
}

var ErrLocationInvalid = errors.New("location is invalid")

func NewLocation(name string) (*Location, error) {
	if !isLocationValid(name) {
		return nil, ErrLocationInvalid
	}

	return &Location{
		name: name,
	}, nil
}

func isLocationValid(name string) bool {
	return name != ""
}

func (l Location) Name() string {
	return l.name
}
