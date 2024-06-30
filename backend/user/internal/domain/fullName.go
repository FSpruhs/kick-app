package domain

import "fmt"

type FullName struct {
	firstName string
	lastName  string
}

var ErrInvalidFullName = fmt.Errorf("invalid full name")

func NewFullName(firstName, lastName string) (*FullName, error) {
	if !isFullNameValid(firstName, lastName) {
		return nil, ErrInvalidFullName
	}
	return &FullName{firstName: firstName, lastName: lastName}, nil
}

func (f FullName) FirstName() string {
	return f.firstName
}

func (f FullName) LastName() string {
	return f.lastName
}

func isFullNameValid(firstName, lastName string) bool {
	return 40 > len(firstName) && len(firstName) > 0 && 40 > len(lastName) && len(lastName) > 0
}
