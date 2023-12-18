package config

import (
	"os"
)

func EnvMongoURI() string {
	return os.Getenv("DATABASE_URL")
}

func DatabaseName() string {
	return os.Getenv("DATABASE_NAME")
}
