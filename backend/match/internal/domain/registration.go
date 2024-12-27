package domain

import "time"

type Registration struct {
	UserID    string
	Accepted  bool
	timeStamp time.Time
}
