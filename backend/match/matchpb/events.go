package matchpb

const MatchCreatedEvent = "match.MatchCreated"

type MatchCreated struct {
	MatchID string
	GroupID string
}
