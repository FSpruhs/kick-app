package ddd

import "github.com/stretchr/testify/mock"

type MockEventPublisher struct {
	mock.Mock
}

var _ EventPublisher[AggregateEvent] = (*MockEventPublisher)(nil)

func (m *MockEventPublisher) Publish(events ...AggregateEvent) error {
	args := m.Called(events)
	return args.Error(0)
}
