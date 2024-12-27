package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"

	"github.com/FSpruhs/kick-app/backend/internal/ginconfig"
	"github.com/FSpruhs/kick-app/backend/internal/rpc"
)

type AppConfig struct {
	EnvMongoURI  string
	DatabaseName string
	RPC          rpc.Config
	Gin          ginconfig.Config
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
		Gin: ginconfig.Config{
			Port:           os.Getenv("GIN_PORT"),
			RealmConfigURL: os.Getenv("REALM_CONFIG_URL"),
			ClientID:       os.Getenv("CLIENT_ID"),
		},
	}
}
