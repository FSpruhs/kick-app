package domain

type MatchRepository interface {
	Save(match *Match) error
}
