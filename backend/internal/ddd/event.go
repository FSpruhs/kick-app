package ddd

type Event interface {
	EventName() string
}

type EventHandler func(event Event) error
