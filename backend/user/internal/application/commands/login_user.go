package commands

import "github.com/FSpruhs/kick-app/backend/user/internal/domain"

type LoginUser struct {
	Email    *domain.Email
	Password *domain.Password
}

type LoginUserHandler struct {
	domain.UserRepository
}

func NewLoginUserHandler(users domain.UserRepository) LoginUserHandler {
	return LoginUserHandler{users}
}

func (h LoginUserHandler) LoginUser(cmd LoginUser) (*domain.User, error) {
	user, err := h.UserRepository.FindByEmail(cmd.Email)
	if err != nil {
		return nil, err
	}

	err = user.Login(cmd.Email, cmd.Password)
	if err != nil {
		return nil, err
	}

	return user, nil
}