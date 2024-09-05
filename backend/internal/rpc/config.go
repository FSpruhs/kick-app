package rpc

import "fmt"

type Config struct {
	Host string `default:"localhost"`
	Port string `default:"8085"`
}

func (c Config) Address() string {
	return fmt.Sprintf("%s:%s", c.Host, c.Port)
}
