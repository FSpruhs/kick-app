package domain

import (
	"strings"
)

type InvalidPlayerRoleError struct {
	role string
}

func (e InvalidPlayerRoleError) Error() string {
	return "invalid player role: " + e.role
}

type PlayerRole int

const (
	Member = iota
	Admin
	Master
)

func ToPlayerRole(role string) (PlayerRole, error) {
	switch strings.ToLower(role) {
	case "member":
		return Member, nil
	case "admin":
		return Admin, nil
	case "master":
		return Master, nil
	default:
		return -1, InvalidPlayerRoleError{role}
	}
}
