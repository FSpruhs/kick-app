package ddd

type (
	AggregateNamer interface {
		AggregateName() string
	}

	Eventer interface {
		AddEvent(string, EventPayload)
		Events() []AggregateEvent
		ClearEvents()
	}

	Aggregate struct {
		Entity
		events []AggregateEvent
	}

	AggregateEvent interface {
		Event
		AggregateID() string
		AggregateName() string
		AggregateVersion() int
	}

	aggregateEvent struct {
		event
	}

	AggregateBase struct {
		ID     string
		events []Event
	}
)

var _ interface {
	AggregateNamer
	Eventer
} = (*Aggregate)(nil)

func (a *AggregateBase) AddEvent(event Event) {
	a.events = append(a.events, event)
}

func (a *AggregateBase) GetEvents() []Event {
	return a.events
}

func (a *AggregateBase) GetID() string {
	return a.ID
}

func NewAggregate(id, name string) Aggregate {
	return Aggregate{
		Entity: NewEntity(id, name),
		events: make([]AggregateEvent, 0),
	}
}

func (a *Aggregate) AggregateName() string {
	return a.name
}

func (a *Aggregate) Events() []AggregateEvent {
	return a.events
}

func (a *Aggregate) ClearEvents() {
	a.events = make([]AggregateEvent, 0)
}

func (a *Aggregate) AddEvent(name string, payload EventPayload) {
	a.events = append(
		a.events,
		&aggregateEvent{
			event: NewEvent(name, payload),
		})
}

func (a *Aggregate) setEvents(events []AggregateEvent) {
	a.events = events
}

func (e aggregateEvent) AggregateID() string {
	return e.id
}

func (e aggregateEvent) AggregateName() string {
	return e.name
}

func (e aggregateEvent) AggregateVersion() int {
	return 1
}
