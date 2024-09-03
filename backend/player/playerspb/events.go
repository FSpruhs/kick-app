package playerspb

const (
	NewMasterAppointedEvent = "player.NewMasterAppointed"
)

type NewMasterAppointed struct {
	OldMasterID string
	NewMasterID string
}
