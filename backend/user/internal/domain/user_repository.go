package domain

import "github.com/FSpruhs/kick-app/backend/user/internal/application/queries"

type UserRepository interface {
	Create(user *User) (*User, error)
	Save(user *User) error
	CountByEmail(email *Email) (int, error)
	FindByEmail(email *Email) (*User, error)
	FindByID(id string) (*User, error)
	FindByIDs(ids []string) ([]*User, error)
	FindAll(filter *queries.Filter) ([]*User, error)
}
