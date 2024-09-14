package getgroupdetails

type Response struct {
	ID          string  `json:"id"`
	Name        string  `json:"name"`
	Users       []*User `json:"users"`
	InviteLevel string  `json:"inviteLevel"`
}

type User struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}
