package createuser

type Response struct {
	ID        string
	FirstName string
	LastName  string
	Email     string
	NickName  string
	Groups    []string
}
