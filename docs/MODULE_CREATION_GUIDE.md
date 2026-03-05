# Module Creation Guide

This guide explains how to create a new module (domain) in the kick-app backend system.

## Overview

The kick-app backend uses a modular monolith architecture. Each module represents a bounded context in the domain and follows Domain-Driven Design (DDD) principles.

## Module Structure

A typical module has the following structure:

```
backend/
└── <module-name>/
    ├── module.go                 # Module registration and startup
    ├── <module-name>pb/          # Protocol Buffers definitions (optional)
    │   └── <module-name>_api.proto
    └── internal/                 # Internal implementation
        ├── application/          # Application layer
        │   ├── application.go    # Main application service
        │   ├── commands/         # Command handlers
        │   └── queries/          # Query handlers
        ├── domain/              # Domain layer
        │   ├── <entity>.go      # Domain entities
        │   ├── <entity>_test.go # Domain tests
        │   └── <entity>_repository.go # Repository interface
        ├── mongodb/             # MongoDB implementation
        │   └── <entity>_repository.go
        ├── grpc/                # gRPC layer
        │   └── server.go        # gRPC server implementation
        ├── rest/                # REST API layer
        │   ├── routes.go        # Route definitions
        │   └── controller/      # HTTP controllers
        └── handler/             # Event handlers
            └── <event>_handler.go
```

## Step-by-Step Guide

### 1. Create the Module Directory

Create a new directory under `backend/` with your module name:

```bash
mkdir -p backend/<module-name>/internal/{application,domain,mongodb,grpc,rest,handler}
```

### 2. Define the Module Interface

Create `backend/<module-name>/module.go`:

```go
package <module-name>

import (
    "fmt"
    
    "github.com/FSpruhs/kick-app/backend/internal/monolith"
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/application"
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/grpc"
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/mongodb"
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
    // Initialize repository
    repository := mongodb.NewRepository(mono.DB(), "<module-name>.<collection>")
    
    // Initialize application service
    app := application.New(repository)
    
    // Register REST routes
    rest.Routes(mono.Router(), app)
    
    // Register gRPC server (if needed)
    if err := grpc.RegisterServer(app, mono.RPC()); err != nil {
        return fmt.Errorf("register <module-name> server: %w", err)
    }
    
    return nil
}
```

### 3. Create Domain Entities

Create `backend/<module-name>/internal/domain/<entity>.go`:

```go
package domain

import (
    "time"
    
    "go.mongodb.org/mongo-driver/bson/primitive"
)

type <Entity> struct {
    ID        primitive.ObjectID `bson:"_id,omitempty"`
    // Add your domain fields here
    CreatedAt time.Time `bson:"createdAt"`
    UpdatedAt time.Time `bson:"updatedAt"`
}

func New<Entity>() *<Entity> {
    return &<Entity>{
        ID:        primitive.NewObjectID(),
        CreatedAt: time.Now(),
        UpdatedAt: time.Now(),
    }
}
```

### 4. Define Repository Interface

Create `backend/<module-name>/internal/domain/<entity>_repository.go`:

```go
package domain

import (
    "context"
    
    "go.mongodb.org/mongo-driver/bson/primitive"
)

type <Entity>Repository interface {
    Save(ctx context.Context, entity *<Entity>) error
    FindByID(ctx context.Context, id primitive.ObjectID) (*<Entity>, error)
    // Add other repository methods as needed
}
```

### 5. Implement MongoDB Repository

Create `backend/<module-name>/internal/mongodb/<entity>_repository.go`:

```go
package mongodb

import (
    "context"
    
    "go.mongodb.org/mongo-driver/bson"
    "go.mongodb.org/mongo-driver/bson/primitive"
    "go.mongodb.org/mongo-driver/mongo"
    
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/domain"
)

type <entity>Repository struct {
    collection *mongo.Collection
}

func NewRepository(db *mongo.Database, collectionName string) domain.<Entity>Repository {
    return &<entity>Repository{
        collection: db.Collection(collectionName),
    }
}

func (r *<entity>Repository) Save(ctx context.Context, entity *domain.<Entity>) error {
    _, err := r.collection.InsertOne(ctx, entity)
    return err
}

func (r *<entity>Repository) FindByID(ctx context.Context, id primitive.ObjectID) (*domain.<Entity>, error) {
    var entity domain.<Entity>
    err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&entity)
    if err != nil {
        return nil, err
    }
    return &entity, nil
}
```

### 6. Create Application Service

Create `backend/<module-name>/internal/application/application.go`:

```go
package application

import (
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/domain"
)

type App struct {
    repository domain.<Entity>Repository
}

func New(repository domain.<Entity>Repository) *App {
    return &App{
        repository: repository,
    }
}
```

### 7. Add REST Routes

Create `backend/<module-name>/internal/rest/routes.go`:

```go
package rest

import (
    "github.com/gin-gonic/gin"
    
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/application"
)

func Routes(router *gin.Engine, app *application.App) {
    v1 := router.Group("/api/v1/<module-name>")
    {
        // Add your routes here
        // v1.POST("/", controller.Create)
        // v1.GET("/:id", controller.Get)
    }
}
```

### 8. Register the Module

Update `backend/cmd/kickapp/main.go` to include your new module:

```go
import (
    // ... other imports
    "github.com/FSpruhs/kick-app/backend/<module-name>"
)

// In the run() function, add your module to the modules slice:
modules := []monolith.Module{
    &player.Module{},
    &user.Module{},
    &group.Module{},
    &<module-name>.Module{},  // Add your module here
}
```

### 9. Add gRPC Support (Optional)

If your module needs gRPC communication:

1. Create a `.proto` file in `backend/<module-name>/<module-name>pb/`
2. Generate Go code using:
   ```bash
   cd backend/<module-name>/<module-name>pb
   protoc --go_out=. --go_opt=module=github.com/FSpruhs/kick-app/backend/<module-name>/<module-name>pb \
          --go-grpc_out=. --go-grpc_opt=module=github.com/FSpruhs/kick-app/backend/<module-name>/<module-name>pb \
          <module-name>_api.proto
   ```

### 10. Add Event Handlers (Optional)

If your module needs to respond to events from other modules:

Create `backend/<module-name>/internal/application/<event>_handler.go`:

```go
package application

import (
    "context"
    
    "github.com/FSpruhs/kick-app/backend/internal/ddd"
)

type <Event>Handler struct {
    // Add dependencies
}

func New<Event>Handler() *<Event>Handler {
    return &<Event>Handler{}
}

func (h *<Event>Handler) Handle(ctx context.Context, event ddd.AggregateEvent) error {
    // Handle the event
    return nil
}
```

Register the handler in `module.go`:

```go
handler.Register<Event>Handler(eventHandler, mono.EventDispatcher())
```

## Testing

Add tests for your domain logic:

Create `backend/<module-name>/internal/domain/<entity>_test.go`:

```go
package domain

import (
    "testing"
    
    "github.com/stretchr/testify/assert"
)

func Test<Entity>Creation(t *testing.T) {
    entity := New<Entity>()
    assert.NotNil(t, entity)
    assert.NotEqual(t, primitive.NilObjectID, entity.ID)
}
```

## Code Quality

### Linting

Run the linter to ensure code quality:

```bash
golangci-lint run ./backend/...
```

### Import Sorting

Sort imports using:

```bash
gci write -s standard -s default -s "prefix(github.com/FSpruhs/kick-app)" ./backend
```

## Example: Existing Modules

Refer to these existing modules for examples:

- **Player Module**: Simple module with basic CRUD operations
- **User Module**: Module with event handlers and gRPC communication
- **Group Module**: Module with complex business logic and event publishing
- **Match Module**: Module with external dependencies

## Best Practices

1. **Follow DDD principles**: Keep domain logic in the domain layer
2. **Separate concerns**: Use the layered architecture (domain, application, infrastructure)
3. **Use interfaces**: Define repository interfaces in the domain layer
4. **Test domain logic**: Write tests for your domain entities and business rules
5. **Handle errors**: Use proper error wrapping with `fmt.Errorf`
6. **Use dependency injection**: Pass dependencies through constructors
7. **Follow naming conventions**: Use consistent naming across modules
8. **Document your code**: Add comments for exported types and functions

## Common Patterns

### Command Pattern

For operations that modify state:

```go
// backend/<module-name>/internal/application/commands/create_entity.go
package commands

import (
    "context"
    
    "github.com/FSpruhs/kick-app/backend/<module-name>/internal/domain"
)

type CreateCommand struct {
    // Command fields
}

func (a *App) Create(ctx context.Context, cmd CreateCommand) error {
    entity := domain.New<Entity>()
    // Set entity properties from command
    return a.repository.Save(ctx, entity)
}
```

### Query Pattern

For read operations:

```go
// backend/<module-name>/internal/application/queries/get_entity.go
package queries

import (
    "context"
    
    "go.mongodb.org/mongo-driver/bson/primitive"
)

type GetQuery struct {
    ID primitive.ObjectID
}

func (a *App) Get(ctx context.Context, query GetQuery) (*domain.<Entity>, error) {
    return a.repository.FindByID(ctx, query.ID)
}
```

## Troubleshooting

### Module not loading
- Ensure the module is added to the `modules` slice in `main.go`
- Check that the `Startup` method doesn't return an error
- Verify all imports are correct

### Database connection issues
- Check MongoDB connection string in `.env`
- Ensure the collection name is unique across modules
- Verify database permissions

### gRPC errors
- Ensure proto files are generated correctly
- Check that the gRPC server is registered
- Verify the RPC address configuration

## References

- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [Go Project Layout](https://github.com/golang-standards/project-layout)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)
- [MongoDB Go Driver](https://www.mongodb.com/docs/drivers/go/current/)
