package ddd

type EventSubscriber interface {
	Subscribe(event Event, handler EventHandler)
}

type EventPublisher interface {
	Publish(events ...Event) error
}

type EventDispatcher struct {
	handlers map[string][]EventHandler
}

var _ interface {
	EventSubscriber
	EventPublisher
} = (*EventDispatcher)(nil)

func NewEventDispatcher() *EventDispatcher {
	return &EventDispatcher{
		handlers: make(map[string][]EventHandler),
	}
}

func (d *EventDispatcher) Subscribe(event Event, handler EventHandler) {
	d.handlers[event.EventName()] = append(d.handlers[event.EventName()], handler)
}

func (d *EventDispatcher) Publish(events ...Event) error {
	for _, event := range events {
		for _, handler := range d.handlers[event.EventName()] {
			if err := handler(event); err != nil {
				return err
			}
		}
	}
	return nil
}
