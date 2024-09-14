package domain

type MessageType int

const (
	GroupInvitation = iota
)

func (mt MessageType) String() string {
	switch mt {
	case GroupInvitation:
		return "groupInvitation"
	default:
		return "unknown"
	}
}
