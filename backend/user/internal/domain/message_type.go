package domain

type MessageType int

const (
	GroupInvitation = iota
	RemovedFromGroup
	MatchInvitation
)

func (mt MessageType) String() string {
	switch mt {
	case GroupInvitation:
		return "groupInvitation"
	case RemovedFromGroup:
		return "removedFromGroup"
	case MatchInvitation:
		return "matchInvitation"
	default:
		return "unknown"
	}
}
