package getuserall

type Response struct {
	ID       string `json:"id"`
	Email    string `json:"email"`
	NickName string `json:"nickName"`
}
