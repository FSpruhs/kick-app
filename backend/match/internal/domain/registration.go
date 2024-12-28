package domain

import "time"

type Registration struct {
	userID    string
	status    RegistrationStatus
	timeStamp time.Time
}

func (r Registration) UserID() string {
	return r.userID
}

func (r Registration) Status() RegistrationStatus {
	return r.status
}

func (r Registration) TimeStamp() time.Time {
	return r.timeStamp
}
