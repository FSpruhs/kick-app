package createuser

type Response struct {
	Id        string
	FirstName string
	LastName  string
	Email     string
	NickName  string
	Groups    []string
}
