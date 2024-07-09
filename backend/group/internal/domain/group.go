package domain

type Group struct {
	Id    string
	Name  string
	Users []string
}

func (g *Group) SetId(id string) {
	g.Id = id
}
