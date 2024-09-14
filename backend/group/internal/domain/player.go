package domain

type Player struct {
	userID string
	status Status
	role   Role
}

func NewPlayer(userID string, status Status, role Role) *Player {
	return &Player{
		userID: userID,
		status: status,
		role:   role,
	}
}

func (p *Player) UserID() string {
	return p.userID
}

func (p *Player) Status() Status {
	return p.status
}

func (p *Player) Role() Role {
	return p.role
}
