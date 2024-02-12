package domain

type Player struct {
	Id        string
	FirstName string
	LastName  string
}

func (p *Player) SetId(id string) {
	p.Id = id
}
