package domain

type Player struct {
	Id      string
	GroupId string
	UserId  string
}

func (p *Player) SetId(id string) {
	p.Id = id
}
