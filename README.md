# Backend Actions

## Create Swagger Documentation
```sh
swag init -g ./backend/cmd/kickapp/main.go -o backend/cmd/docs
```

## Sort Imports
```sh
gci write -s standard -s default -s "prefix(github.com/FSpruhs/kick-app)" ./backend
```