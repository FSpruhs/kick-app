package domain

type RegistrationStatus int

const (
	Registered = iota
	Deregistered
	Removed
	Added
	AddedWhenFull
)

func (rs RegistrationStatus) String() string {
	return [...]string{"Registered", "Deregistered", "Removed", "Added", "AddedWhenFull"}[rs]
}
