# Copilot Coding Agent Instructions for `kick-app`

This file covers everything needed to work efficiently in this repository. The **primary, actively developed backend** is `backend-kotlin/`. The frontend is `frontend/`. The `backend/` directory contains a legacy Go backend that is archived and no longer developed.

---

## Repository Layout

```
kick-app/
├── .github/workflows/       # CI/CD (java.yml, docker.yml, go.yml)
├── .infra/keycloak/         # Keycloak realm import JSON
├── backend-kotlin/          # ← PRIMARY BACKEND (Kotlin + Spring Boot 3.5)
├── backend/                 # Legacy Go backend (archived – do not modify)
├── frontend/                # Vue 3 + TypeScript SPA
├── docs/                    # Architecture diagrams (SVG / Excalidraw)
├── docker-compose.yml       # Local dev infrastructure
└── README.md                # Project overview (written in German)
```

---

## Backend (`backend-kotlin/`)

### Overview

Kotlin + Spring Boot 3.5 service for managing football (soccer/futsal) groups, players, and matches. Uses **Event Sourcing + CQRS** backed by PostgreSQL (write side) and MongoDB (read/view side), structured with [Spring Modulith](https://spring.io/projects/spring-modulith).

- **Framework**: Spring Boot 3.5, Spring WebFlux (reactive/non-blocking), Kotlin Coroutines, Project Reactor.
- **Write side**: PostgreSQL via R2DBC + Flyway. Events in `kick_app.events` (partitioned by `aggregate_id`). Snapshots in `kick_app.snapshots`. Migrations in `src/main/resources/db/migration/`.
- **Read side**: MongoDB via Spring Data MongoDB Reactive.
- **Auth**: Keycloak OAuth2 (profile `oauth2`, default) **or** custom HS256 JWT filter (profile `jwtSecurity`, development only — use the Keycloak profile for production).
- **File storage**: MinIO.
- **Server port**: `8085` (configured in `application.yml`; tests use `server.port: 0`).

### Spring Modulith Modules

Packages under `com.spruhs.kick_app`:

| Module    | Purpose                                                                                         |
|-----------|-------------------------------------------------------------------------------------------------|
| `group`   | Group management (create, invite, player roles/status)                                          |
| `match`   | Match scheduling and results                                                                    |
| `user`    | User registration, authentication, profile images (MinIO)                                       |
| `message` | Internal messaging/notifications                                                                |
| `view`    | Read-side projections (MongoDB) consumed by REST views                                          |
| `common`  | Shared infrastructure: event sourcing base classes, exceptions, security configs, value objects |

**Inter-module communication**: Spring application events (Spring Modulith event bus). Each module exposes its public API events in an `api` sub-package (e.g. `group/api/GroupEvents.kt`). Cross-module dependencies must only go through these `api` packages. Direct module-to-module calls are not allowed.

Each module must have a `package-info.java` in its `core` and `api` packages to define Spring Modulith boundaries.

### Build and Test Commands

```bash
cd backend-kotlin

# Run all tests (Testcontainers spins up MongoDB automatically; needs Docker daemon)
mvn test

# Build without running tests
mvn package -DskipTests

# Run locally (needs PostgreSQL + MongoDB + Keycloak running via docker-compose)
mvn spring-boot:run

# Run with JWT security profile (no Keycloak required; development only)
# jwt.secret must be at least 32 characters (256 bits) for HS256
mvn spring-boot:run -Dspring-boot.run.profiles=jwtSecurity -Djwt.secret=<your-secret-min-32-chars>
```

### Important Test Notes

- Tests use [Testcontainers](https://testcontainers.com/) for MongoDB — a running **Docker daemon is required**.
- PostgreSQL is **disabled** in tests via `application-test.yml` (`spring.r2dbc.url: disabled`, `spring.flyway.enabled: false`).
- Security is bypassed in tests via `TestSecurityConfig.kt`.
- The CI workflow provides a real PostgreSQL 15 service container and sets `TESTCONTAINERS_RYUK_DISABLED=true`.
- Test profiles use `application-test.yml` which sets `server.port: 0` (random port).
- Base class for MongoDB integration tests: `BaseMongoDBTest.kt`.

### Domain Concepts

- **Group**: A football group with a list of players. Players have a `PlayerRole` (COACH or PLAYER) and a `PlayerStatus` (Active, Inactive, Leaved, Removed). A COACH can invite, remove, promote/demote players.
- **Match**: A game scheduled within a group. Players respond with availability.
- **Player**: Embedded within the `Group` aggregate (not a separate top-level aggregate).
- **User**: Application user. Registration involves creating a Keycloak account. Profile image stored in MinIO.
- **Message**: Internal notification/mailbox entry triggered by group/match events.
- **View/Projection**: MongoDB read-side documents updated by listening to domain events. Used for fast queries.

### Code Conventions

- **Domain model**: Aggregates extend `AggregateRoot` from `common/es/EventSourcing.kt`. Domain logic lives in `core/domain/`. Use `apply(event)` to record and apply an event.
- **Event sourcing**: `AggregateStoreImpl` saves/loads aggregates. Serialization is done by `Serializer` implementations (one per aggregate type). `SerializerFactory` routes by aggregate class simple name.
- **Use cases / application layer**: Interface-based ports in `core/application/`. Primary adapters (REST, event listeners) in `core/adapter/primary/`. Secondary adapters (DB, external services) in `core/adapter/secondary/`.
- **REST controllers**: Spring WebFlux `@RestController`, all reactive (suspend functions or returning `Mono`/`Flux`).
- **Value objects**: Inline (`@JvmInline value class`) for IDs and constrained strings (e.g. `UserId`, `Name`).
- **Shared types**: Common enums and IDs live in `common/types/`.
- **Exceptions**: Custom exception classes in `common/exceptions/Exceptions.kt`.
- **Logging**: Use the `getLogger()` helper from `common/helper/Logger.kt`.
- **Test framework**: JUnit 5 + MockK (`io.mockk:mockk`) for mocking + AssertJ for assertions + `reactor-test` StepVerifier for reactive streams. Test files follow the same package structure as main code.

### Spring Profiles

| Profile | Config file | Notes |
|---|---|---|
| *(default)* | `application.yml` | Connects to local PostgreSQL (5432), MongoDB (6000), Keycloak (8080) |
| `dev` | `application-dev.yml` | Development overrides |
| `docker` | `application-docker.yml` | Docker environment overrides |
| `jwtSecurity` | `application-jwtSecurity.yml` | Enables custom JWT filter instead of Keycloak OAuth2 |

### Key Backend Files

| File | Purpose |
|---|---|
| `src/main/kotlin/com/spruhs/kick_app/common/es/EventSourcing.kt` | Core event sourcing infrastructure (`AggregateRoot`, `AggregateStoreImpl`, `Serializer`) |
| `src/main/kotlin/com/spruhs/kick_app/common/es/Events.kt` | Base event publisher |
| `src/main/kotlin/com/spruhs/kick_app/common/configs/SecurityConfig.kt` | Security filter chains and JWT utilities |
| `src/main/resources/application.yml` | Main app configuration |
| `src/main/resources/db/migration/V1__initial_setup.sql` | PostgreSQL schema (event store + snapshots) |
| `src/test/resources/application-test.yml` | Test configuration (disables R2DBC/Flyway, random port) |
| `src/test/kotlin/com/spruhs/kick_app/TestSecurityConfig.kt` | Disables security in tests |
| `src/test/kotlin/com/spruhs/kick_app/BaseMongoDBTest.kt` | Base class for MongoDB integration tests (Testcontainers) |

---

## Frontend (`frontend/`)

### Overview

Vue 3 + TypeScript SPA using Vite as bundler, Pinia for state management, Vue Router 4 for routing, Axios for HTTP, Vuetify for UI components, and Keycloak JS for authentication.

**Dev server**: `http://localhost:5173` · **Backend URL**: `http://localhost:8085`

### Build and Test Commands

```bash
cd frontend

npm install

npm run dev          # Start dev server
npm run build        # Production build (runs type-check + vite build)
npm run test:unit    # Run unit tests with Vitest
npm run lint         # ESLint fix + Prettier format
npm run format       # Prettier only
```

### Frontend Structure

```
frontend/src/
├── components/      # Reusable Vue components
├── views/           # Page-level components (HomeView, GroupView, MatchDetailsView, etc.)
├── router/          # Vue Router configuration
├── store/           # Pinia stores (AuthStore, GroupStore, MessageStore)
├── services/        # HTTP clients (axiosService, groupRestService, matchRestService, etc.)
├── model/           # TypeScript interfaces/types
└── main.ts          # App entry point
```

### Frontend Code Conventions

- **Style**: 2-space indent, 100-char print width, single quotes, no semicolons, no trailing commas (see `.prettierrc.json`).
- **Linting**: ESLint with Vue 3 essential + TypeScript + Prettier rules.
- **Tests**: Vitest + Vue Test Utils. Run with `npm run test:unit`.
- **Type checking**: `vue-tsc --build --force` (included in `npm run build`).

---

## Local Infrastructure (docker-compose.yml)

Start all services from the repo root:

```bash
docker-compose up -d
```

| Service | Port(s) | Credentials |
|---------|---------|-------------|
| PostgreSQL 16 | `5432` | `admin` / `password123` (DB: `kick_app`) |
| MongoDB 6.0 | `6000` | `admin` / `password123` (DB: `kick_app`) |
| Keycloak 25 | `8080` | `admin` / `admin` |
| MinIO | `9000` (API), `9001` (Console) | `admin` / `password` |
| MailHog | `1025` (SMTP), `8025` (Web UI) | — |

> ⚠️ These credentials are for **local development only**. Change all passwords before deploying to any non-local environment.

The Keycloak realm (`kick-app`) is auto-imported from `.infra/keycloak/`.

---

## CI / GitHub Actions

### Java CI (`.github/workflows/java.yml`)

Triggers on push/PR to `main` when `backend-kotlin/**` changes:
1. Sets up JDK 21 (Eclipse Temurin).
2. Starts a **PostgreSQL 15** service container.
3. Runs `mvn test` with `TESTCONTAINERS_RYUK_DISABLED=true`.

### Docker Build (`.github/workflows/docker.yml`)

Triggers after successful Java CI on `main`:
1. Builds the Docker image from `backend-kotlin/Dockerfile`.
2. Pushes to Docker Hub as `fspruhs/kick-app-kotlin-backend:latest`.
3. Requires repository secrets: `DOCKER_USERNAME` and `DOCKER_PASSWORD`.

---

## Known Issues and Workarounds

### PostgreSQL version mismatch (CI vs. local)

- **Issue**: CI uses PostgreSQL **15**; `docker-compose.yml` uses PostgreSQL **16**.
- **Workaround**: All Flyway SQL migrations must remain compatible with PostgreSQL 15+. Do not use features exclusive to PostgreSQL 16.

### Testcontainers in CI

- **Issue**: Testcontainers Ryuk process may fail in some CI environments.
- **Workaround**: The Java CI workflow sets `TESTCONTAINERS_RYUK_DISABLED=true` to disable Ryuk. This is already handled in `.github/workflows/java.yml`.

### Running backend tests without Docker

- **Issue**: MongoDB integration tests require a running Docker daemon (for Testcontainers).
- **Workaround**: Ensure Docker is running locally before executing `mvn test`. In environments without Docker, only unit tests (not extending `BaseMongoDBTest`) can run.

### CORS in development

- **Issue**: CORS is configured to allow all origins in the backend.
- **Note**: This is intentional for local development only. Restrict origins in any production deployment.