package domain

type UserRepository interface {
	Create(user *User) (*User, error)
	CountByEmail(email *Email) (int, error)
	FindByEmail(email *Email) (*User, error)
}
