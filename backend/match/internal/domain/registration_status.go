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
