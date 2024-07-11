package ddd

type (
	EventHandler[T Event] interface {
		HandleEvent(event T) error
	}

	EventHandlerFunc[T Event] func(event T) error

	EventSubscriber[T Event] interface {
		Subscribe(name string, handler EventHandler[T])
	}

	EventPublisher[T Event] interface {
		Publish(events ...T) error
	}

	EventDispatcher[T Event] struct {
		handlers map[string][]EventHandler[T]
	}
)

var _ interface {
	EventSubscriber[Event]
	EventPublisher[Event]
} = (*EventDispatcher[Event])(nil)

func NewEventDispatcher[T Event]() *EventDispatcher[T] {
	return &EventDispatcher[T]{
		handlers: make(map[string][]EventHandler[T]),
	}
}

func (d *EventDispatcher[T]) Subscribe(name string, handler EventHandler[T]) {
	d.handlers[name] = append(d.handlers[name], handler)
}

func (d *EventDispatcher[T]) Publish(events ...T) error {
	for _, event := range events {
		for _, handler := range d.handlers[event.EventName()] {
			err := handler.HandleEvent(event)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

func (f EventHandlerFunc[T]) HandleEvent(event T) error {
	return f(event)
}
