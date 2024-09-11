# Backend Actions

## Create Swagger Documentation
```sh
swag init -g ./backend/cmd/kickapp/main.go -o ./backend/cmd/docs
```

## Sort Imports
```sh
gci write -s standard -s default -s "prefix(github.com/FSpruhs/kick-app)" ./backend
```

## Create Proto go files for player
```sh
cd backend/player/playerspb
protoc --go_out=. --go_opt=module=github.com/FSpruhs/kick-app/backend/player/playerspb --go-grpc_out=. --go-grpc_opt=module=github.com/FSpruhs/kick-app/backend/player/playerspb api.proto
```

## Run golangci-lint
```sh
golangci-lint run ./backend/...
```