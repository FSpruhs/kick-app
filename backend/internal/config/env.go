package config

import (
	"log"
	"os"

	"github.com/FSpruhs/kick-app/backend/internal/rpc"
	"github.com/joho/godotenv"
)

type AppConfig struct {
	EnvMongoURI  string
	DatabaseName string
	RPC          rpc.Config
}

func InitConfig() AppConfig {
	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env.file")
	}

	return AppConfig{
		EnvMongoURI:  os.Getenv("DATABASE_URL"),
		DatabaseName: os.Getenv("DATABASE_NAME"),
		RPC: rpc.Config{
			Port: os.Getenv("GRPC_PORT"),
			Host: os.Getenv("GRPC_HOST"),
		},
	}
}
