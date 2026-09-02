# Task Manager API

A multi-tenant task management REST API built with **Spring Boot** and **Spring Security**, featuring JWT-based authentication, role-based access control, and organization-scoped data isolation. Built as a portfolio project to demonstrate backend fundamentals: secure auth, relational data modeling, and clean REST design.

## Features

- **JWT Authentication** — stateless login/register flow using Spring Security and JSON Web Tokens
- **Role-Based Access Control** — admin-only actions (creating projects/tasks) enforced via `@PreAuthorize`
- **Multi-Tenant Data Isolation** — users belong to an organization; all data (projects, tasks) is scoped to the current user's organization
- **Full CRUD** — complete create/read/update/delete on projects and tasks; create/read/delete on comments
- **Custom Exception Handling** — `ResourceNotFoundException` and `AccessDeniedException` for clean, meaningful error responses
- **Dashboard Aggregation** — summary endpoint reporting total projects, total tasks, task counts by status, and overdue task counts
- **Interactive API Docs** — Swagger UI / OpenAPI 3 documentation with built-in Bearer token authorization
- **Dockerized** — runs via Docker Compose alongside a MySQL container

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot 4 |
| Security | Spring Security, JWT |
| Database | MySQL |
| ORM | Hibernate / Spring Data JPA |
| API Docs | springdoc-openapi (Swagger UI) |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |

## Data Model

```
organizations
    └── users (belongs to an organization; has a role, e.g. ADMIN/USER)
            └── projects (created by a user, scoped to an organization)
                    └── tasks (belongs to a project; optional assignee)
                            └── comments (belongs to a task; has an author)
```

All foreign keys enforce referential integrity, and access checks ensure users can only see/modify data within their own organization.

## Getting Started

### Prerequisites

- Java 21+ (or whatever JDK version your `pom.xml` targets)
- Docker & Docker Compose
- Maven (or use the included `./mvnw` wrapper — no local install needed)

### Run with Docker Compose

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

The API will be available at `http://localhost:8080`.

### API Documentation

Once running, open:

```
http://localhost:8080/swagger-ui/index.html
```

to explore and test all endpoints interactively. Protected endpoints require a Bearer token — click **Authorize** in the Swagger UI, paste your JWT (obtained from `/api/auth/login`), and all subsequent requests will include it automatically.

## API Overview

### Auth
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user + organization | No |
| POST | `/api/auth/login` | Log in, returns a JWT | No |

### Projects
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/projects` | List projects in your organization | Yes |
| POST | `/api/projects` | Create a project | Yes (Admin) |
| PUT | `/api/projects/{id}` | Update a project | Yes |
| DELETE | `/api/projects/{id}` | Delete a project | Yes |

### Tasks
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/tasks?projectId={id}` | List tasks for a project | Yes |
| POST | `/api/tasks` | Create a task | Yes (Admin) |
| PUT | `/api/tasks/{id}` | Update a task (status, priority, assignee, etc.) | Yes |
| DELETE | `/api/tasks/{id}` | Delete a task | Yes |

### Comments
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/comments?taskId={id}` | List comments on a task | Yes |
| POST | `/api/comments` | Add a comment to a task | Yes |
| DELETE | `/api/comments/{id}` | Delete a comment | Yes |

### Dashboard
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/dashboard` | Org-wide summary: total projects, total tasks, tasks by status, overdue count | Yes |

## Authentication Flow

1. `POST /api/auth/register` with `firstName`, `lastName`, `email`, `password`, `organizationName` to create an account (this also creates a new organization).
2. `POST /api/auth/login` with `email` and `password` to receive a JWT.
3. Include the token on all subsequent requests:
   ```
   Authorization: Bearer <token>
   ```
