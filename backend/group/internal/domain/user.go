package domain

type User struct {
	id     string
	name   string
	role   string
	status string
}

func NewUser(id, name string) *User {
	return &User{id: id, name: name, role: "", status: ""}
}

func (u *User) ID() string {
	return u.id
}

func (u *User) Name() string {
	return u.name
}

func (u *User) Role() string {
	return u.role
}

func (u *User) Status() string {
	return u.status
}

func (u *User) SetRole(role string) {
	u.role = role
}

func (u *User) SetStatus(status string) {
	u.status = status
}
