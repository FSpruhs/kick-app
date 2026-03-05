# Copilot Coding Agent Instructions

## Repository Overview

**kick-app** is a full-stack web application for managing football (soccer/futsal) groups, players, and matches. It consists of three sub-projects:

| Directory | Language / Stack | Purpose |
|---|---|---|
| `backend/` | Go 1.22 | Legacy Go backend (Gin, MongoDB, gRPC) |
| `backend-kotlin/` | Kotlin + Spring Boot 3.5 | Active Kotlin backend (event-sourced, PostgreSQL + MongoDB) |
| `frontend/` | Vue 3 + TypeScript | Web frontend (Vuetify, Pinia, Keycloak auth) |

The **Kotlin backend** (`backend-kotlin/`) is the primary, actively developed backend. The Go backend (`backend/`) is legacy and should not receive new feature work unless explicitly requested.

---

## Architecture

### Kotlin Backend (`backend-kotlin/`)

- **Framework**: Spring Boot 3.5 with Kotlin coroutines and Project Reactor (WebFlux / reactive stack).
- **Persistence (write side)**: PostgreSQL via R2DBC + Flyway migrations. Events are stored in `kick_app.events` (partitioned by `aggregate_id`). Snapshots are stored in `kick_app.snapshots`. Schema lives in `src/main/resources/db/migration/`.
- **Persistence (read/view side)**: MongoDB via Spring Data MongoDB Reactive.
- **Architecture pattern**: Event Sourcing + CQRS with [Spring Modulith](https://spring.io/projects/spring-modulith).
- **Authentication**: Keycloak (OAuth2 resource server, profile `oauth2`) **or** a custom JWT-based filter (profile `jwtSecurity`).
- **Modules** (Spring Modulith packages inside `com.spruhs.kick_app`):
  - `group` – group management (create, invite, player roles/status)
  - `match` – match scheduling and results
  - `user` – user registration, authentication, profile images (MinIO)
  - `message` – internal messaging/notifications
  - `view` – read-side projections (MongoDB) consumed by REST views
  - `common` – shared infrastructure: event sourcing base classes, exceptions, security configs, value objects
- **Inter-module communication**: Spring application events (Spring Modulith event bus). Each module exposes its public API events in an `api` sub-package (e.g. `group/api/GroupEvents.kt`). Cross-module dependencies go through these `api` packages only.
- **File image storage**: MinIO.

### Go Backend (`backend/`)

- **Framework**: Gin (HTTP) + gRPC (inter-module communication).
- **Database**: MongoDB.
- **Modules**: `group`, `user`, `player`, `match`.
- **Entry point**: `cmd/kickapp/main.go`.
- **Swagger docs**: auto-generated in `cmd/docs/`.

### Frontend (`frontend/`)

- Vue 3 (Composition API with `<script setup>`) + TypeScript.
- UI library: Vuetify 3 with Material Design Icons (`@mdi/font`).
- State management: Pinia (with `pinia-plugin-persistedstate`).
- HTTP client: Axios.
- Authentication: Keycloak via `@josempgon/vue-keycloak` and `keycloak-js`.
- Routing: `vue-router`.

### Infrastructure

- `docker-compose.yml` at repo root starts all dependencies: MongoDB (port 6000→27017), PostgreSQL (5432), Keycloak (8080), MailHog (1025/8025), MinIO (9000/9001).
- Keycloak realm config is in `.infra/keycloak/v20/imports/`.

---

## How to Build and Test

### Kotlin Backend

```bash
cd backend-kotlin

# Run all tests (requires nothing external – Testcontainers spins up MongoDB automatically)
mvn test

# Build (skip tests)
mvn package -DskipTests

# Run locally (needs PostgreSQL + MongoDB + Keycloak running via docker-compose)
mvn spring-boot:run
```

**Important test notes:**
- Tests use [Testcontainers](https://testcontainers.com/) for MongoDB (see `TestcontainersConfiguration.kt` and `BaseMongoDBTest.kt`).
- PostgreSQL in tests is disabled via `application-test.yml` (`spring.r2dbc.url: disabled`, `spring.flyway.enabled: false`).
- Security is bypassed in tests via `TestSecurityConfig.kt`.
- The CI workflow (`java.yml`) provides a real PostgreSQL 15 service container and sets `TESTCONTAINERS_RYUK_DISABLED=true`.
- Test profiles use `application-test.yml` which sets `server.port: 0` (random port).

### Go Backend

```bash
cd backend

# Install/tidy dependencies
go mod tidy

# Build
go build -v ./...

# Run tests
go test -v ./...

# Lint (golangci-lint required)
golangci-lint run
```

- Go version: the `go.mod` file requires **Go 1.22**, but the CI workflow (`go.yml`) currently pins `go-version: '1.21'`. This mismatch may cause CI failures; the CI should be updated to use Go 1.22.
- Linter config: `.golangci.yml` (strict — most linters enabled; all linters are disabled for `_test.go` files).

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Type-check
npm run type-check

# Build
npm run build

# Unit tests (Vitest)
npm run test:unit

# Lint (ESLint + fix)
npm run lint

# Format (Prettier)
npm run format
```

---

## Domain Concepts

- **Group**: A football group with a list of players. Players have a `PlayerRole` (COACH or PLAYER) and a `PlayerStatus` (Active, Inactive, Leaved, Removed). A COACH can invite, remove, promote/demote players.
- **Match**: A game scheduled within a group. Players respond with availability.
- **Player**: Within the Go backend, `player` is a separate aggregate. In the Kotlin backend, player data is embedded within the `Group` aggregate.
- **User**: Application user. Registration involves creating a Keycloak account. Profile image stored in MinIO.
- **Message**: Internal notification/mailbox entry triggered by group/match events.
- **View/Projection**: MongoDB read-side documents updated by listening to domain events. Used for fast queries.

---

## Kotlin Backend Code Conventions

- **Domain model**: Aggregates extend `AggregateRoot` from `common/es/EventSourcing.kt`. Domain logic lives in `core/domain/`. Use `apply(event)` to record and apply an event.
- **Event sourcing**: `AggregateStoreImpl` saves/loads aggregates. Serialization is done by `Serializer` implementations (one per aggregate type). `SerializerFactory` routes by aggregate class simple name.
- **Use cases / application layer**: Interface-based ports in `core/application/`. Primary adapters (REST, event listeners) are in `core/adapter/primary/`. Secondary adapters (DB, external services) are in `core/adapter/secondary/`.
- **REST controllers**: Spring WebFlux `@RestController`, all reactive (suspend functions or returning `Mono`/`Flux`).
- **Value objects**: Inline (`@JvmInline value class`) for IDs and constrained strings (e.g. `UserId`, `Name`).
- **Shared types**: Common enums and IDs live in `common/types/`.
- **Exceptions**: Custom exception classes in `common/exceptions/Exceptions.kt`.
- **Logging**: Use the `getLogger()` helper from `common/helper/Logger.kt`.
- **Spring Modulith**: Each module must have a `package-info.java` in its `core` and `api` packages to define module boundaries. Cross-module access is only through the `api` package.
- **Test framework**: JUnit 5 + MockK (`io.mockk:mockk`) for mocking + AssertJ for assertions + `reactor-test` StepVerifier for reactive streams. Test files follow the same package structure as main code.

## Go Backend Code Conventions

- Each domain module (`group`, `user`, `player`, `match`) has a `module.go` at its root defining a `Module` struct with a `Startup(mono monolith.Monolith) error` method.
- Internal packages: `internal/application` (use cases), `internal/domain` (domain model + repository interfaces), `internal/grpc` (gRPC adapters), `internal/mongodb` (MongoDB adapters), `internal/rest` (REST handlers).
- gRPC proto definitions are compiled to `internal/grpc/`.
- Swagger annotations are on REST handlers; run `swag init` to regenerate `cmd/docs/`.
- Tests use `github.com/stretchr/testify`.
- Mock repositories are committed (e.g. `mock_group_repository.go` generated by mockery).

---

## Environment Configuration

- Root `.env` file (not committed) and `backend/.env` hold MongoDB/Postgres credentials used by `docker-compose.yml`.
- Kotlin backend profiles:
  - Default (no profile): plain Spring Boot config from `application.yml` (connects to local PostgreSQL on 5432, MongoDB on 6000, Keycloak on 8080).
  - `dev`: `application-dev.yml`
  - `docker`: `application-docker.yml`
  - `jwtSecurity`: `application-jwtSecurity.yml` – enables custom JWT filter instead of Keycloak OAuth2.
- Frontend `.env` configures API base URL and Keycloak details.

---

## CI / GitHub Actions

| Workflow | File | Trigger | What it does |
|---|---|---|---|
| `Go` | `.github/workflows/go.yml` | Push/PR to `main` affecting `backend/**` | `go mod tidy` → `go build` → `go test` |
| `Java` | `.github/workflows/java.yml` | Push/PR to `main` affecting `backend-kotlin/**` | Starts PostgreSQL service → `mvn test` |
| `Docker` | `.github/workflows/docker.yml` | `Java` workflow succeeds on `main` | Builds and pushes Docker image to Docker Hub (`fspruhs/kick-app-kotlin-backend:latest`) |

### Known CI issues / workarounds

- **Go version mismatch**: The Go CI workflow (`go.yml`) pins `go-version: '1.21'`, but `backend/go.mod` declares `go 1.22`. This can cause CI failures. Fix: update `go.yml` to use `go-version: '1.22'`.
- The Docker workflow requires `DOCKER_USERNAME` and `DOCKER_PASSWORD` repository secrets to be set.
- `TESTCONTAINERS_RYUK_DISABLED=true` is set in the Java CI workflow to avoid Docker-in-Docker issues with Testcontainers Ryuk container.
- PostgreSQL 15 is used in CI but the local `docker-compose.yml` uses PostgreSQL 16. Migrations must be compatible with PostgreSQL 15+.

---

## Key Files Quick Reference

| File | Purpose |
|---|---|
| `backend-kotlin/src/main/kotlin/com/spruhs/kick_app/common/es/EventSourcing.kt` | Core event sourcing infrastructure |
| `backend-kotlin/src/main/kotlin/com/spruhs/kick_app/common/es/Events.kt` | Base event publisher |
| `backend-kotlin/src/main/kotlin/com/spruhs/kick_app/common/configs/SecurityConfig.kt` | Security filter chains and JWT utilities |
| `backend-kotlin/src/main/resources/application.yml` | Main app configuration |
| `backend-kotlin/src/main/resources/db/migration/V1__initial_setup.sql` | PostgreSQL schema (event store + snapshots) |
| `backend-kotlin/src/test/resources/application-test.yml` | Test configuration (disables R2DBC/Flyway) |
| `backend/cmd/kickapp/main.go` | Go backend entry point |
| `backend/.golangci.yml` | Go linter configuration |
| `frontend/src/main.ts` | Frontend entry point |
| `frontend/src/router/index.ts` | Vue Router configuration |
| `docker-compose.yml` | Local infrastructure (MongoDB, PostgreSQL, Keycloak, MinIO, MailHog) |
