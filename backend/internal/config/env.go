package config

import (
	"github.com/FSpruhs/kick-app/backend/internal/rpc"
	"log"
	"os"

	"github.com/joho/godotenv"
)

type AppConfig struct {
	EnvMongoURI  string
	DatabaseName string
	Rpc          rpc.RpcConfig
}

func InitConfig() AppConfig {
	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env.file")
	}

	return AppConfig{
		EnvMongoURI:  os.Getenv("DATABASE_URL"),
		DatabaseName: os.Getenv("DATABASE_NAME"),
		Rpc: rpc.RpcConfig{
			Port: os.Getenv("GRPC_PORT"),
			Host: os.Getenv("GRPC_HOST"),
		},
	}
}
