package domain

import "strings"

type InvalidPlayerRoleError struct {
	role string
}

func (e InvalidPlayerRoleError) Error() string {
	return "invalid player role: " + e.role
}

type Role int

const (
	Member = iota
	Admin
	Master
)

func ToRole(role string) (Role, error) {
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

func (r Role) String() string {
	switch r {
	case Member:
		return "member"
	case Admin:
		return "admin"
	case Master:
		return "master"
	default:
		return "unknown"
	}
}
