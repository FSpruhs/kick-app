# Agent/Modul erstellen - Schnellstart

Diese Anleitung zeigt, wie man einen neuen Agent (Modul/Domain) im kick-app Backend erstellt.

## Was ist ein "Agent"?

In diesem System bezieht sich "Agent" auf ein **Modul** oder eine **Domain** - einen eigenständigen Bereich der Anwendung, der eine bestimmte Geschäftslogik verwaltet.

Beispiele für bestehende Module/Agents:
- **Player**: Verwaltet Spielerprofile
- **User**: Verwaltet Benutzerkonten
- **Group**: Verwaltet Sportgruppen
- **Match**: Koordiniert Spielplanung

## Schnellstart

### 1. Modulverzeichnis erstellen

```bash
mkdir -p backend/<modul-name>/internal/{application,domain,mongodb,grpc,rest,handler}
```

### 2. Module-Datei erstellen (`module.go`)

Erstelle `backend/<modul-name>/module.go`:

```go
package <modul-name>

import (
    "fmt"
    "github.com/FSpruhs/kick-app/backend/internal/monolith"
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/application"
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/mongodb"
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/rest"
)

type Module struct{}

func (m *Module) Startup(mono monolith.Monolith) error {
    repository := mongodb.NewRepository(mono.DB(), "<modul-name>.<collection>")
    app := application.New(repository)
    rest.Routes(mono.Router(), app)
    return nil
}
```

### 3. Domain-Entity erstellen

Erstelle `backend/<modul-name>/internal/domain/<entity>.go`:

```go
package domain

import (
    "time"
    "go.mongodb.org/mongo-driver/bson/primitive"
)

type <Entity> struct {
    ID        primitive.ObjectID `bson:"_id,omitempty"`
    Name      string             `bson:"name"`
    CreatedAt time.Time          `bson:"createdAt"`
    UpdatedAt time.Time          `bson:"updatedAt"`
}

func New<Entity>(name string) *<Entity> {
    return &<Entity>{
        ID:        primitive.NewObjectID(),
        Name:      name,
        CreatedAt: time.Now(),
        UpdatedAt: time.Now(),
    }
}
```

### 4. Repository-Interface definieren

Erstelle `backend/<modul-name>/internal/domain/<entity>_repository.go`:

```go
package domain

import (
    "context"
    "go.mongodb.org/mongo-driver/bson/primitive"
)

type <Entity>Repository interface {
    Save(ctx context.Context, entity *<Entity>) error
    FindByID(ctx context.Context, id primitive.ObjectID) (*<Entity>, error)
}
```

### 5. MongoDB Repository implementieren

Erstelle `backend/<modul-name>/internal/mongodb/<entity>_repository.go`:

```go
package mongodb

import (
    "context"
    "go.mongodb.org/mongo-driver/bson"
    "go.mongodb.org/mongo-driver/bson/primitive"
    "go.mongodb.org/mongo-driver/mongo"
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/domain"
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

### 6. Application Service erstellen

Erstelle `backend/<modul-name>/internal/application/application.go`:

```go
package application

import (
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/domain"
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

### 7. REST API Routes hinzufügen

Erstelle `backend/<modul-name>/internal/rest/routes.go`:

```go
package rest

import (
    "github.com/gin-gonic/gin"
    "github.com/FSpruhs/kick-app/backend/<modul-name>/internal/application"
)

func Routes(router *gin.Engine, app *application.App) {
    v1 := router.Group("/api/v1/<modul-name>")
    {
        // Füge hier deine Routen hinzu
        // v1.POST("/", controller.Create)
        // v1.GET("/:id", controller.Get)
    }
}
```

### 8. Modul registrieren

**WICHTIG**: Füge dein Modul in `backend/cmd/kickapp/main.go` hinzu:

```go
import (
    // ... andere Imports
    "github.com/FSpruhs/kick-app/backend/<modul-name>"
)

// In der run() Funktion:
modules := []monolith.Module{
    &player.Module{},
    &user.Module{},
    &group.Module{},
    &match.Module{},
    &<modul-name>.Module{},  // Füge dein Modul hier hinzu
}
```

### 9. Testen und Bauen

```bash
# Backend bauen
cd backend
go build ./cmd/kickapp/main.go

# Linter ausführen
golangci-lint run ./...

# Imports sortieren
gci write -s standard -s default -s "prefix(github.com/FSpruhs/kick-app)" ./backend
```

## Detaillierte Dokumentation

Für eine vollständige, detaillierte Anleitung mit mehr Beispielen, siehe:

**[📚 Module Creation Guide (Englisch)](MODULE_CREATION_GUIDE.md)**

Diese ausführliche Anleitung enthält:
- Schritt-für-Schritt-Anleitungen
- Code-Beispiele
- Best Practices
- Event Handlers
- gRPC Integration
- Testing
- Fehlerbehebung

## Beispiele

Schaue dir die bestehenden Module an:
- `backend/player/` - Einfaches Modul
- `backend/user/` - Modul mit Event Handlers
- `backend/group/` - Modul mit komplexer Logik
- `backend/match/` - Modul mit externen Abhängigkeiten

## Fragen?

Bei Fragen zur Modul-Erstellung, siehe die vollständige [Module Creation Guide](MODULE_CREATION_GUIDE.md) Dokumentation.
