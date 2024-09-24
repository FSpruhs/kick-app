package domain

import "strings"

type InvalidStatusError struct {
	role string
}

func (e InvalidStatusError) Error() string {
	return "invalid player status: " + e.role
}

type Status int

const (
	Active = iota
	Inactive
	Leaved
	Removed
	NotFound
)

func ToStatus(status string) (Status, error) {
	switch strings.ToLower(status) {
	case "active":
		return Active, nil
	case "inactive":
		return Inactive, nil
	case "leaved":
		return Leaved, nil
	case "removed":
		return Removed, nil
	case "not_found":
		return NotFound, nil
	default:
		return -1, InvalidStatusError{status}
	}
}

func (s Status) String() string {
	switch s {
	case Active:
		return "active"
	case Inactive:
		return "inactive"
	case Leaved:
		return "leaved"
	case Removed:
		return "removed"
	case NotFound:
		return "not found"
	default:
		return "unknown"
	}
}
