package domain

type PlayerRepository interface {
	Create(player *Player) (*Player, error)
	FindByID(id string) (*Player, error)
	FindByUserIDAndGroupID(userID, groupID string) (*Player, error)
	Save(player *Player) error
	SaveAll(players []*Player) error
}
