package domain

type UserRepository interface {
	Create(user *User) (*User, error)
	CountByEmail(email *Email) (int, error)
	FindByEmail(email *Email) (*User, error)
	FindByID(id string) (*User, error)
	FindByIDs(ids []string) ([]*User, error)
}
