# Copilot Coding Agent Instructions for `backend-kotlin`

This file covers everything needed to work efficiently in the `backend-kotlin/` directory, which is the **primary, actively developed backend** of the kick-app project.

## Overview

`backend-kotlin` is a Kotlin + Spring Boot 3.5 service for managing football (soccer/futsal) groups, players, and matches. It uses **Event Sourcing + CQRS** backed by PostgreSQL (write side) and MongoDB (read/view side), structured with [Spring Modulith](https://spring.io/projects/spring-modulith).

---

## Architecture

- **Framework**: Spring Boot 3.5 with Kotlin coroutines and Project Reactor (WebFlux / reactive stack).
- **Persistence (write side)**: PostgreSQL via R2DBC + Flyway migrations. Events are stored in `kick_app.events` (partitioned by `aggregate_id`). Snapshots are stored in `kick_app.snapshots`. Schema lives in `src/main/resources/db/migration/`.
- **Persistence (read/view side)**: MongoDB via Spring Data MongoDB Reactive.
- **Authentication**: Keycloak (OAuth2 resource server, profile `oauth2`) **or** a custom JWT-based filter (profile `jwtSecurity`).
- **File image storage**: MinIO.

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

**Inter-module communication**: Spring application events (Spring Modulith event bus). Each module exposes its public API events in an `api` sub-package (e.g. `group/api/GroupEvents.kt`). Cross-module dependencies must only go through these `api` packages.

Each module must have a `package-info.java` in its `core` and `api` packages to define Spring Modulith boundaries.

---

## How to Build and Test

```bash
cd backend-kotlin

# Run all tests (Testcontainers spins up MongoDB automatically)
mvn test

# Build without running tests
mvn package -DskipTests

# Run locally (needs PostgreSQL + MongoDB + Keycloak running)
mvn spring-boot:run
```

**Important test notes:**
- Tests use [Testcontainers](https://testcontainers.com/) for MongoDB (see `TestcontainersConfiguration.kt` and `BaseMongoDBTest.kt`).
- PostgreSQL is disabled in tests via `application-test.yml` (`spring.r2dbc.url: disabled`, `spring.flyway.enabled: false`).
- Security is bypassed in tests via `TestSecurityConfig.kt`.
- The CI workflow (`java.yml`) provides a real PostgreSQL 15 service container and sets `TESTCONTAINERS_RYUK_DISABLED=true`.
- Test profiles use `application-test.yml` which sets `server.port: 0` (random port).

---

## Domain Concepts

- **Group**: A football group with a list of players. Players have a `PlayerRole` (COACH or PLAYER) and a `PlayerStatus` (Active, Inactive, Leaved, Removed). A COACH can invite, remove, promote/demote players.
- **Match**: A game scheduled within a group. Players respond with availability.
- **Player**: Embedded within the `Group` aggregate (not a separate top-level aggregate).
- **User**: Application user. Registration involves creating a Keycloak account. Profile image stored in MinIO.
- **Message**: Internal notification/mailbox entry triggered by group/match events.
- **View/Projection**: MongoDB read-side documents updated by listening to domain events. Used for fast queries.

---

## Code Conventions

- **Domain model**: Aggregates extend `AggregateRoot` from `common/es/EventSourcing.kt`. Domain logic lives in `core/domain/`. Use `apply(event)` to record and apply an event.
- **Event sourcing**: `AggregateStoreImpl` saves/loads aggregates. Serialization is done by `Serializer` implementations (one per aggregate type). `SerializerFactory` routes by aggregate class simple name.
- **Use cases / application layer**: Interface-based ports in `core/application/`. Primary adapters (REST, event listeners) in `core/adapter/primary/`. Secondary adapters (DB, external services) in `core/adapter/secondary/`.
- **REST controllers**: Spring WebFlux `@RestController`, all reactive (suspend functions or returning `Mono`/`Flux`).
- **Value objects**: Inline (`@JvmInline value class`) for IDs and constrained strings (e.g. `UserId`, `Name`).
- **Shared types**: Common enums and IDs live in `common/types/`.
- **Exceptions**: Custom exception classes in `common/exceptions/Exceptions.kt`.
- **Logging**: Use the `getLogger()` helper from `common/helper/Logger.kt`.
- **Test framework**: JUnit 5 + MockK (`io.mockk:mockk`) for mocking + AssertJ for assertions + `reactor-test` StepVerifier for reactive streams. Test files follow the same package structure as main code.

---

## Environment Configuration

Spring profiles:

| Profile | Config file | Notes |
|---|---|---|
| *(default)* | `application.yml` | Connects to local PostgreSQL (5432), MongoDB (6000), Keycloak (8080) |
| `dev` | `application-dev.yml` | Development overrides |
| `docker` | `application-docker.yml` | Docker environment overrides |
| `jwtSecurity` | `application-jwtSecurity.yml` | Enables custom JWT filter instead of Keycloak OAuth2 |

Local infrastructure (MongoDB, PostgreSQL, Keycloak, MinIO, MailHog) is started via `docker-compose.yml` at the repo root.

---

## CI / GitHub Actions

The `Java` workflow (`.github/workflows/java.yml`) triggers on push/PR to `main` affecting `backend-kotlin/**`:
- Starts a PostgreSQL 15 service container.
- Runs `mvn test` with `TESTCONTAINERS_RYUK_DISABLED=true`.

On success on `main`, the `Docker` workflow builds and pushes the Docker image to `fspruhs/kick-app-kotlin-backend:latest`. The Docker workflow requires `DOCKER_USERNAME` and `DOCKER_PASSWORD` repository secrets.

**Known CI issue**: PostgreSQL 15 is used in CI but the local `docker-compose.yml` uses PostgreSQL 16. Migrations must remain compatible with PostgreSQL 15+.

---

## Key Files Quick Reference

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