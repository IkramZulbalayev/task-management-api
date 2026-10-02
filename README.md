# Task Manager

A full-stack, multi-tenant task management application built with **Java, Spring Boot, React, TypeScript, MySQL, and Docker**.

The project demonstrates secure authentication, role-based authorization, organization-scoped data isolation, REST API design, frontend-backend integration, automated testing, and containerized deployment.

---

## Features

### Authentication & Security

- JWT-based authentication
- User registration and login
- Role-based access control
- Admin-only project and task creation
- Protected frontend routes
- CORS configuration for frontend-backend communication

### Multi-Tenant Data Isolation

Each user belongs to an organization.

All projects, tasks, comments, and dashboard data are scoped to the authenticated user's organization.

Users cannot access or modify resources belonging to another organization.

This isolation is enforced in the service layer and covered by automated unit tests.

### Projects

- Create projects
- View organization projects
- Update projects
- Delete projects
- View project-specific tasks

### Tasks

- Create tasks
- Update tasks
- Delete tasks
- Assign tasks to organization members
- Set task priority
- Change task status
- Set due dates
- Enforce organization-level authorization

A task cannot be assigned to a user from another organization.

### Comments

- Add comments to tasks
- View task comments
- Delete comments
- Organization-scoped access

### Dashboard

Organization-wide dashboard with:

- Total projects
- Total tasks
- Tasks grouped by status
- Overdue task count

---

## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Java | Backend language |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build tool |
| Swagger / OpenAPI | API documentation |

### Frontend

| Technology | Purpose |
|---|---|
| React | UI framework |
| TypeScript | Type-safe frontend development |
| Axios | HTTP communication |
| React Router | Client-side routing |
| CSS | Application styling |

### Testing

| Technology | Purpose |
|---|---|
| JUnit | Unit testing |
| Mockito | Mocking dependencies |
| AssertJ | Fluent assertions |

### DevOps

- Docker
- Docker Compose

---

## Architecture

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

The service layer contains:

- Business logic
- Authorization checks
- Organization isolation
- Resource validation

The frontend communicates with the backend through REST APIs.

```text
React + TypeScript
        ↓
     REST API
        ↓
 Spring Boot
        ↓
 Spring Security
        ↓
 Service Layer
        ↓
 Spring Data JPA
        ↓
      MySQL
```

---

## Domain Model

```text
Organization
    └── Users
          └── Projects
                └── Tasks
                      └── Comments
```

Main relationships:

- A user belongs to an organization
- A project belongs to an organization
- A project is created by a user
- A task belongs to a project
- A task may be assigned to a user
- A comment belongs to a task
- A comment has an author

All organization-owned data is protected from cross-tenant access.

---

## Frontend

The frontend is built with **React + TypeScript** and consumes the Spring Boot REST API end-to-end.

It includes:

- Login page
- Registration page
- Dashboard
- Project list
- Project details
- Task management
- Task details
- Comments
- User profile
- Protected routes
- Reusable UI components
- Role-aware UI behavior

### Frontend Screenshots

#### Project Management — Admin

Admins can create and manage organization projects.

<img width="1920" height="1001" alt="Task manager" src="https://github.com/user-attachments/assets/d917dc98-3c94-4395-a06d-fe85a96ee5f3" />


#### Task Management — Admin

Admins can create tasks with a title, description, due date, and priority.

<img width="1920" height="997" alt="Task manager" src="https://github.com/user-attachments/assets/cdfe37d5-52f3-49b2-891a-121c8ccf949f" />


#### Task Details — Admin

Administrators can view task information, edit or delete tasks, and manage comments.

<img width="1920" height="996" alt="Task manager" src="https://github.com/user-attachments/assets/6e55b222-968f-4d53-8bd6-80370ffc3658" />


#### Member View

Regular organization members can view projects available within their organization while administrative actions remain restricted.

<img width="1920" height="1004" alt="Task manager" src="https://github.com/user-attachments/assets/24991277-5358-47b9-b3b2-ba30463e2dff" />


#### Task Comments

Organization members can collaborate by adding comments to tasks.

<img width="1920" height="998" alt="Task manager" src="https://github.com/user-attachments/assets/b827ca71-21af-499d-9ff3-60d7ed0c67ea" />


---

## Automated Testing

The project currently includes **36 passing tests** using:

- JUnit
- Mockito
- AssertJ

Tests cover:

- Authentication
- Organization management
- Projects
- Tasks
- Comments
- CRUD operations
- Authorization rules
- Error scenarios
- Multi-tenant data isolation
- Invalid cross-organization task assignment

### Multi-Tenant Test Example

```text
User from Organization A
        ↓
Access Project from Organization A
        ✅ Allowed

User from Organization A
        ↓
Access Project from Organization B
        ❌ Denied
```

### Cross-Organization Assignment Example

```text
Task belongs to Organization A
        ↓
Assign user from Organization B
        ❌ Denied
```

This verifies that organization isolation is enforced by the backend rather than relying only on frontend restrictions.

### Test Coverage Examples

The test suite includes cases such as:

- Successful authentication
- Invalid authentication attempts
- Creating projects successfully
- Rejecting access to projects from another organization
- Creating tasks successfully
- Preventing cross-organization task access
- Preventing assignment of users from another organization
- Updating tasks
- Deleting tasks
- Adding and deleting comments
- Handling missing resources
- Verifying repository interactions with Mockito

### Test Results

All current tests pass successfully.

<img width="1855" height="320" alt="Task manager" src="https://github.com/user-attachments/assets/cfb03402-fdcc-4425-a899-e41859fb77ac" />

---

## API Overview

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register user and organization |
| POST | `/api/auth/login` | Authenticate and receive JWT |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects` | Get organization projects |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks?projectId={id}` | Get tasks for project |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |

### Comments

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/comments?taskId={id}` | Get task comments |
| POST | `/api/comments` | Add comment |
| DELETE | `/api/comments/{id}` | Delete comment |

### Dashboard

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard` | Get organization dashboard statistics |

---

## Authentication Flow

```text
Register / Login
      ↓
Backend validates credentials
      ↓
JWT generated
      ↓
Frontend stores token
      ↓
Token sent with protected requests
```

Protected requests include:

```http
Authorization: Bearer <token>
```

If the API returns `401 Unauthorized`, the frontend clears the session and redirects the user to the login page.

---

## Running the Backend

### Requirements

- Java 21+
- Maven
- Docker
- Docker Compose

### Build the Backend

```bash
./mvnw clean package
```

### Run with Docker

```bash
docker compose up --build
```

### Backend URL

```text
http://localhost:8080
```

### Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Running the Frontend

### Open the Frontend Directory

```bash
cd frontend
```

### Install Dependencies

```bash
npm install
```

### Start the Frontend

```bash
npm run dev
```

The frontend will usually be available at:

```text
http://localhost:5173
```

Make sure the backend is running on:

```text
http://localhost:8080
```

---

## Project Structure

```text
task-management-api/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/project/taskmanager/
│   │           ├── auth/
│   │           ├── comment/
│   │           ├── dashboard/
│   │           ├── organization/
│   │           ├── project/
│   │           ├── security/
│   │           ├── task/
│   │           └── user/
│   │
│   └── test/
│       └── java/
│           └── com/project/taskmanager/service/
│               ├── AuthServiceTest.java
│               ├── CommentServiceTest.java
│               ├── OrganizationServiceTest.java
│               ├── ProjectServiceTest.java
│               └── TaskServiceTest.java
│
├── frontend/
│   ├── screenshots/
│   │   ├── create-project.png
│   │   ├── create-task.png
│   │   ├── task-details-admin.png
│   │   ├── projects-member.png
│   │   ├── task-comments-member.png
│   │   ├── all-tests-passing.png
│   │   ├── task-service-tests.png
│   │   └── project-service-tests.png
│   │
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── types/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   │
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Key Backend Concepts Demonstrated

- REST API design
- JWT authentication
- Spring Security
- Role-based authorization
- Service-layer authorization
- Multi-tenancy
- Relational database modeling
- Spring Data JPA
- Dependency injection
- DTO-based API responses
- Exception handling
- CORS
- Unit testing
- Mocking with Mockito
- Dockerized deployment
- Frontend-backend integration

---

## Future Improvements

Possible future additions:

- Refresh tokens
- Task filtering and pagination
- Task search
- Task history / audit log
- Notifications
- More advanced role management
- GitHub Actions CI
- Integration tests
- Frontend Docker container
- Production deployment

---

## About

This project was built as a portfolio and university project to demonstrate full-stack development with a strong focus on **Java backend engineering, security, multi-tenant architecture, automated testing, and REST API design**.
