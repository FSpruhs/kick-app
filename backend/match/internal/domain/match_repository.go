package domain

type MatchRepository interface {
	Save(match *Match) error
	FindByID(id string) (*Match, error)
}
