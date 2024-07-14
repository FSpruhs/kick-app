package application

type App interface {
	Commands
	Queries
}

type Commands interface{}

type Queries interface{}

type Application struct{ appCommands }

type appCommands struct{}

var _ App = (*Application)(nil)
