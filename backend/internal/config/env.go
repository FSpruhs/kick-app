package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"

	"github.com/FSpruhs/kick-app/backend/internal/rpc"
)

type AppConfig struct {
	EnvMongoURI  string
	DatabaseName string
	GinPort      string
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
		GinPort: os.Getenv("GIN_PORT"),
	}
}
