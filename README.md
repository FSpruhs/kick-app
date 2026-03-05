# ⚽ Kick App

> Eine Webanwendung zur Verwaltung von Fußball- und Futsal-Gruppen, Spielern und Matches.

[![Java CI](https://github.com/fspruhs/kick-app/actions/workflows/java.yml/badge.svg)](https://github.com/fspruhs/kick-app/actions/workflows/java.yml)
[![Docker](https://github.com/fspruhs/kick-app/actions/workflows/docker.yml/badge.svg)](https://github.com/fspruhs/kick-app/actions/workflows/docker.yml)
[![Docker Image](https://img.shields.io/docker/v/fspruhs/kick-app-kotlin-backend?label=Docker%20Hub&logo=docker)](https://hub.docker.com/r/fspruhs/kick-app-kotlin-backend)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-42b883?logo=vue.js&logoColor=white)](https://vuejs.org/)

---

## Inhaltsverzeichnis

- [Übersicht](#übersicht)
- [Architektur](#architektur)
- [Tech Stack](#tech-stack)
- [Projektstruktur](#projektstruktur)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Spring Modulith Module](#spring-modulith-module)
- [Spring Profile](#spring-profile)
- [Sicherheit](#sicherheit)
- [Tests](#tests)
- [CI/CD](#cicd)
- [Wichtige Dateien](#wichtige-dateien)

---

## Übersicht

**Kick App** hilft Fußball- und Futsal-Communities dabei, ihre Gruppen und Matches zu organisieren. Trainer können Gruppen anlegen, Spieler einladen, Spiele planen und die Verfügbarkeit der Spieler verfolgen. Das Backend basiert auf **CQRS + Event Sourcing** und bietet damit eine vollständige, unveränderliche Änderungshistorie.

---

## Architektur

```
┌──────────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3)                          │
│              TypeScript · Pinia · Axios · Keycloak JS        │
└───────────────────────────┬──────────────────────────────────┘
                            │ REST (HTTP/JSON)
┌───────────────────────────▼──────────────────────────────────┐
│              Backend — Spring Boot 3.5 (Kotlin)              │
│                Spring WebFlux · Spring Modulith              │
│                                                              │
│  ┌──────────┐  ┌─────────┐  ┌────────┐  ┌─────────────┐    │
│  │  group   │  │  match  │  │  user  │  │   message   │    │
│  └────┬─────┘  └────┬────┘  └───┬────┘  └──────┬──────┘    │
│       └─────────────┴───────────┴───────────────┘           │
│                 Spring Modulith Event Bus                    │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  common — Event Sourcing · Security · Value Objects   │   │
│  └───────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────┐                                 │
│  │   view (CQRS Read Side) │                                 │
│  │   MongoDB Projections   │                                 │
│  └────────────┬────────────┘                                 │
└───────────────┼──────────────────────┬───────────────────────┘
                │                      │
  ┌─────────────▼──────────┐  ┌────────▼────────────────┐
  │   MongoDB 6 (Read)     │  │  PostgreSQL 16 (Write)  │
  │   View-Projektionen    │  │  kick_app.events        │
  │   für schnelle Queries │  │  kick_app.snapshots     │
  └────────────────────────┘  └─────────────────────────┘
            ┌────────────┐        ┌──────────────┐
            │   MinIO    │        │   Keycloak   │
            │  (Images)  │        │    (Auth)    │
            └────────────┘        └──────────────┘
```

| Seite | Technologie | Aufgabe |
|-------|-------------|---------|
| **Write** | PostgreSQL 16 via R2DBC + Flyway | Speichert unveränderliche Domain-Events (`kick_app.events`) und Snapshots (`kick_app.snapshots`) |
| **Read** | MongoDB 6 via Spring Data Reactive | Denormalisierte View-Dokumente, die durch Domain-Events aktualisiert werden |
| **Kommunikation** | Spring Modulith Application Events | Entkoppelte Inter-Modul-Kommunikation – keine direkten Abhängigkeiten zwischen Modulen |

---

## Tech Stack

### Backend (`backend-kotlin/`) — primär, aktiv entwickelt

| Kategorie | Technologie |
|-----------|------------|
| Sprache | Kotlin (JDK 21) |
| Framework | Spring Boot 3.5, Spring WebFlux, Spring Modulith |
| Write-DB | PostgreSQL 16, R2DBC, Flyway |
| Read-DB | MongoDB 6 |
| Auth | Keycloak 25 (OAuth2) · Custom JWT Filter (HS256) |
| Dateispeicher | MinIO |
| Async | Kotlin Coroutines, Project Reactor |
| Tests | JUnit 5, MockK, AssertJ, Testcontainers |
| Build | Maven (`./mvnw` Wrapper) |

### Frontend (`frontend/`)

| Kategorie | Technologie |
|-----------|------------|
| Sprache | TypeScript |
| Framework | Vue 3, Vite |
| State | Pinia |
| Routing | Vue Router 4 |
| HTTP | Axios |
| Auth | Keycloak JS |
| Tests | Vitest, Vue Test Utils |

### Infrastruktur

| Service | Port(s) | Zugangsdaten |
|---------|---------|--------------|
| PostgreSQL 16 | `5432` | `kick_app` / `kick_app` |
| MongoDB 6.0 | `6000` | — |
| Keycloak 25.0.1 | `8080` | `admin` / `admin` |
| MinIO | `9000` (API), `9001` (Console) | `minioadmin` / `minioadmin` |
| MailHog | `1025` (SMTP), `8025` (Web UI) | — |

---

## Projektstruktur

```
kick-app/
├── backend-kotlin/              # Primäres Backend (Kotlin + Spring Boot) ← aktiv entwickelt
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/spruhs/kick_app/
│   │   │   │   ├── common/      # Shared: Event Sourcing, Security, Value Objects
│   │   │   │   ├── group/       # Gruppen-Management-Modul
│   │   │   │   ├── match/       # Match-Planungs-Modul
│   │   │   │   ├── message/     # Internes Nachrichten-Modul
│   │   │   │   ├── user/        # User-Management & Profilbilder
│   │   │   │   └── view/        # Read-Side MongoDB Projektionen
│   │   │   └── resources/
│   │   │       └── db/migration/    # Flyway SQL Migrationen
│   │   └── test/
│   └── pom.xml
├── backend/                     # Legacy Go-Backend (archiviert)
├── frontend/                    # Vue 3 + TypeScript SPA
│   └── src/
│       ├── components/
│       ├── views/
│       ├── store/
│       ├── services/
│       ├── model/
│       └── router/
├── docs/                        # Architekturdiagramme
├── docker-compose.yml           # Lokale Infrastruktur
└── README.md
```

> **Hinweis:** Das Verzeichnis `backend/` enthält das archivierte Go-Backend und wird nicht mehr aktiv entwickelt.

---

## Key Features

- 🏟️ **Gruppen-Management** — Gruppen erstellen, Spieler einladen, Rollen (COACH / PLAYER) vergeben, Status verwalten (Active, Inactive, Leaved, Removed)
- 📅 **Match-Planung** — Spiele innerhalb einer Gruppe planen und Verfügbarkeiten der Spieler erfassen
- 🔔 **Interne Benachrichtigungen** — Nachrichtensystem, das automatisch durch Gruppen- und Match-Events ausgelöst wird
- 👤 **User-Profile** — Registrierung via Keycloak, Profilbild-Upload und Speicherung mit MinIO
- 📜 **Event Sourcing** — Vollständige, unveränderliche Änderungshistorie als Append-Only Event Log in PostgreSQL
- ⚡ **CQRS Read Side** — Schnelle, denormalisierte MongoDB-View-Projektionen für effiziente Queries
- 🔄 **Reaktiver Stack** — Vollständig non-blocking Backend mit Spring WebFlux und Kotlin Coroutines
- 🧩 **Modulares Design** — Spring Modulith erzwingt saubere, verifizierbare Modulgrenzen

---

## Getting Started

### Voraussetzungen

- [Docker](https://www.docker.com/) & Docker Compose
- JDK 21 (z. B. [Eclipse Temurin](https://adoptium.net/))
- Node.js 22+
- Maven oder den mitgelieferten `./mvnw` Wrapper

### 1. Infrastruktur starten

```bash
docker-compose up -d
```

| Service | URL |
|---------|-----|
| Keycloak Admin Console | http://localhost:8080 |
| MinIO Console | http://localhost:9001 |
| MailHog Web UI | http://localhost:8025 |

### 2. Backend starten

```bash
cd backend-kotlin

# Mit Standard-Profil (Keycloak OAuth2)
./mvnw spring-boot:run

# Mit JWT-Security-Profil (kein Keycloak nötig)
./mvnw spring-boot:run -Dspring-boot.run.profiles=jwtSecurity -Djwt.secret=<dein-secret>

# Nur bauen (ohne Tests)
./mvnw package -DskipTests
```

Das Backend startet auf **http://localhost:8081**.

| Einstellung | Wert |
|-------------|------|
| PostgreSQL | `r2dbc:postgresql://localhost:5432/kick_app` |
| MongoDB | `mongodb://localhost:6000/kick_app` |
| Keycloak Issuer | `http://localhost:8080/realms/kick-app` |
| Server Port | `8081` |

### 3. Frontend starten

```bash
cd frontend

npm install
npm run dev
```

Das Frontend startet auf **http://localhost:5173**.

---

## Spring Modulith Module

Alle Module liegen unter `com.spruhs.kick_app`:

| Modul | Aufgabe |
|-------|---------|
| `common` | Shared-Infrastruktur: `AggregateRoot`, Event Sourcing, Exceptions, Security-Konfiguration, Value Objects |
| `group` | Gruppen-Lifecycle — erstellen, Spieler einladen, Rollen & Status verwalten |
| `match` | Match-Planung — Spiele erstellen, Verfügbarkeiten erfassen |
| `user` | User-Registrierung & Auth, Profilbild-Upload via MinIO |
| `message` | Internes Benachrichtigungs- und Postfachsystem |
| `view` | CQRS Read Side — MongoDB-Projektionen für REST-View-Endpoints |

**Kommunikationsregel:** Module kommunizieren ausschließlich über Spring Application Events aus dem jeweiligen `api`-Subpackage (z. B. `group/api/GroupEvents.kt`). Direkte Modul-zu-Modul-Aufrufe sind nicht erlaubt.

---

## Spring Profile

| Profil | Config-Datei | Beschreibung |
|--------|-------------|--------------|
| *(default)* | `application.yml` | Lokale Entwicklung mit Keycloak OAuth2 |
| `dev` | `application-dev.yml` | Entwicklungs-Overrides |
| `docker` | `application-docker.yml` | Docker-Umgebungs-Overrides |
| `jwtSecurity` | `application-jwtSecurity.yml` | Custom JWT Filter (HS256) statt Keycloak. Benötigt Property `jwt.secret`. |

---

## Sicherheit

### Profil `oauth2` (Standard)

Nutzt **Spring Security OAuth2 Resource Server** mit Keycloak als Identity Provider.

- JWT-Validierung gegen `http://localhost:8080/realms/kick-app`
- Öffentlich zugänglich: nur `POST /api/v1/user` (Registrierung)
- Alle anderen Endpunkte erfordern einen gültigen Bearer-Token

### Profil `jwtSecurity`

Nutzt einen eigenen **`JwtAuthenticationWebFilter`** mit **HMAC-SHA256 (HS256)**.

- Benötigt Property `jwt.secret`
- Öffentlich zugänglich: `POST /api/v1/user`, `POST /api/v1/auth/**`, Swagger UI
- Alle anderen Endpunkte erfordern einen gültigen Bearer-Token

> ⚠️ **CORS** ist für alle Origins geöffnet — nur für lokale Entwicklung gedacht. In Produktion einschränken!

---

## Tests

### Backend

```bash
cd backend-kotlin

# Alle Tests ausführen (Testcontainers startet MongoDB automatisch)
./mvnw test
```

| Tool | Zweck |
|------|-------|
| JUnit 5 | Test-Framework |
| MockK | Kotlin-natives Mocking |
| AssertJ | Fluent Assertions |
| Testcontainers | Startet echte MongoDB-Instanz in Docker |
| StepVerifier | Testen von reaktiven Streams |

**Wichtige Test-Konfigurationsdateien:**

| Datei | Zweck |
|-------|-------|
| `application-test.yml` | Deaktiviert PostgreSQL (R2DBC + Flyway), setzt `server.port: 0` |
| `TestSecurityConfig.kt` | Deaktiviert Security in Tests |
| `BaseMongoDBTest.kt` | Basisklasse für MongoDB-Integrationstests (Testcontainers) |

> Tests benötigen einen laufenden **Docker-Daemon** (für Testcontainers).

### Frontend

```bash
cd frontend

npm run test:unit
```

---

## CI/CD

### Java CI (`java.yml`)

Ausgelöst bei **Push oder Pull Request auf `main`** wenn `backend-kotlin/**` betroffen ist.

1. JDK 21 (Eclipse Temurin) einrichten
2. PostgreSQL 15 Service-Container starten
3. `./mvnw -B test` ausführen (`TESTCONTAINERS_RYUK_DISABLED=true`)

> ⚠️ CI nutzt PostgreSQL **15**, lokal läuft PostgreSQL **16**. Alle Flyway-Migrationen müssen mit PostgreSQL 15+ kompatibel bleiben.

### Docker Build & Push (`docker.yml`)

Ausgelöst nach erfolgreichem Java-CI-Workflow auf `main`.

1. Docker-Image aus `backend-kotlin/Dockerfile` bauen
2. Als `fspruhs/kick-app-kotlin-backend:latest` auf Docker Hub pushen

**Benötigte Repository Secrets:** `DOCKER_USERNAME`, `DOCKER_PASSWORD`

```bash
# Aktuelles Backend-Image pullen
docker pull fspruhs/kick-app-kotlin-backend:latest
```

---

## Wichtige Dateien

| Datei | Zweck |
|-------|-------|
| `backend-kotlin/src/main/kotlin/.../common/es/EventSourcing.kt` | Core Event Sourcing (`AggregateRoot`, `AggregateStoreImpl`, `Serializer`) |
| `backend-kotlin/src/main/kotlin/.../common/es/Events.kt` | Basis Event Publisher |
| `backend-kotlin/src/main/kotlin/.../common/configs/SecurityConfig.kt` | Security Filter Chains und JWT Utilities |
| `backend-kotlin/src/main/resources/application.yml` | Haupt-App-Konfiguration |
| `backend-kotlin/src/main/resources/db/migration/V1__initial_setup.sql` | PostgreSQL-Schema (Event Store + Snapshots) |
| `docs/architektur.svg` | Architekturdiagramm (SVG) |
| `docs/architektur.excalidraw` | Architekturdiagramm (editierbar) |

