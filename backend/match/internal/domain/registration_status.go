package domain

type RegistrationStatus int

const (
	Registered = iota
	Deregistered
	Removed
	Added
)

func (rs RegistrationStatus) String() string {
	return [...]string{"Registered", "Deregistered", "Removed", "Added"}[rs]
}

func RegistrationStatusFromString(s string) RegistrationStatus {
	switch s {
	case "Registered":
		return Registered
	case "Deregistered":
		return Deregistered
	case "Removed":
		return Removed
	case "Added":
		return Added
	default:
		return -1
	}
}
