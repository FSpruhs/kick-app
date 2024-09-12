package domain

type UserRepository interface {
	GetUser(userID string) (*User, error)
	GetUserAll(userIDs []string) ([]*User, error)
}
