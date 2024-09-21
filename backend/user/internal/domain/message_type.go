package domain

type MessageType int

const (
	GroupInvitation = iota
	RemovedFromGroup
)

func (mt MessageType) String() string {
	switch mt {
	case GroupInvitation:
		return "groupInvitation"
	case RemovedFromGroup:
		return "removedFromGroup"
	default:
		return "unknown"
	}
}
