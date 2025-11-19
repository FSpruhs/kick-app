# Kick App

A modular monolith application for managing sports groups, players, and matches.

## Architecture

The application follows a modular monolith architecture with Domain-Driven Design (DDD) principles. Each module represents a bounded context:

- **Player**: Manages player profiles and roles
- **User**: Handles user accounts and messaging
- **Group**: Manages sports groups and member invitations
- **Match**: Coordinates match scheduling and participation

## Documentation

- [Module Creation Guide](docs/MODULE_CREATION_GUIDE.md) - Learn how to create a new module (agent/domain) (English)
- [Agent Erstellen](docs/AGENT_ERSTELLEN.md) - Anleitung zum Erstellen eines neuen Moduls (Deutsch)
- [Backend Actions](backend/README.md) - Common backend development tasks
- [Architecture Diagram](docs/architektur.svg) - Visual overview of the system

## Getting Started

### Prerequisites

- Go 1.21 or higher
- MongoDB
- Docker (optional, for containerized deployment)

### Running the Application

1. Configure environment variables (see `.env` files in backend and frontend directories)
2. Start the backend:
   ```bash
   cd backend
   go run cmd/kickapp/main.go
   ```
3. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Development

### Backend

See [backend/README.md](backend/README.md) for:
- Generating Swagger documentation
- Creating Protocol Buffer files
- Running linters
- Sorting imports

### Creating a New Module

To create a new module (domain/agent) in the system, follow the comprehensive guide:

**English:**
- **[📚 Module Creation Guide](docs/MODULE_CREATION_GUIDE.md)** - Comprehensive guide with examples

**Deutsch:**
- **[📚 Agent Erstellen](docs/AGENT_ERSTELLEN.md)** - Schnellstart-Anleitung auf Deutsch

These guides include:
- Step-by-step instructions
- Code examples
- Best practices
- Common patterns
- Troubleshooting

## Project Structure

```
.
├── backend/              # Go backend (modular monolith)
│   ├── cmd/             # Application entrypoints
│   ├── internal/        # Shared internal packages
│   ├── player/          # Player module
│   ├── user/            # User module
│   ├── group/           # Group module
│   └── match/           # Match module
├── frontend/            # Vue.js frontend
├── docs/                # Documentation
└── docker-compose.yml   # Docker composition
```

## License

[Add license information]
