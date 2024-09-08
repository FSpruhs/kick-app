package createuser

type Response struct {
	ID        string   `json:"id"`
	FirstName string   `json:"firstName"`
	LastName  string   `json:"lastName"`
	Email     string   `json:"email"`
	NickName  string   `json:"nickName"`
	Groups    []string `json:"groups"`
}
