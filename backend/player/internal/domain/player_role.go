package domain

import (
	"errors"
	"fmt"
	"strings"
)

var ErrInvalidPlayerRole = errors.New("invalid player role")

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
		return -1, fmt.Errorf("mapping string %s to player role: %w", role, ErrInvalidPlayerRole)
	}
}
