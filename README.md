# Taskflow API

Taskflow API is a Spring Boot backend for managing tasks. It provides CRUD operations, a dedicated endpoint for marking tasks complete, and an AI-generated summary of the current task list.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database
- Spring AI with an OpenAI-compatible provider
- Maven Wrapper

## Prerequisites

- JDK 21 or later
- An OpenAI-compatible API key for the task summary endpoint

## Run locally

From the project root:

```bash
export SPRING_AI_OPENAI_API_KEY=your-api-key
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Frontend CORS

The API allows browser requests to `/api/**` from `http://localhost:4173`
(Vite preview) and `http://localhost:5173` (Vite dev) by default.

On Railway, set `CORS_ALLOWED_ORIGINS` to a comma-separated list of exact
frontend origins, then redeploy the backend. For example:

```text
CORS_ALLOWED_ORIGINS=http://localhost:4173,https://your-frontend.example.com
```

This replaces the default allowlist. Use origins only (scheme, hostname, and
port if needed), with no path or trailing slash. Requests support `GET`, `HEAD`,
`POST`, `PUT`, `DELETE`, and `OPTIONS`, with `Content-Type`, `Authorization`,
and `Accept` headers. Cross-origin cookies are not enabled.

Vite's development proxy is not included in production builds; direct browser
requests to Railway need their frontend origin in this backend allowlist.

## Build and test

```bash
./mvnw clean test
./mvnw package
```

Run the packaged application with:

```bash
java -jar target/taskflow-api-0.0.1-SNAPSHOT.jar
```

## API endpoints

All task endpoints are under `/api/tasks`.

| Method | Path | What it does |
| --- | --- | --- |
| `GET` | `/api/tasks` | Get all tasks |
| `GET` | `/api/tasks/{id}` | Get one task by ID |
| `POST` | `/api/tasks` | Create a new task |
| `PUT` | `/api/tasks/{id}` | Update an existing task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `PUT` | `/api/tasks/{id}/complete` | Mark a task as complete |
| `GET` | `/api/tasks/summary` | Generate an AI summary of all tasks |

## Global exception handling

The API uses a global `@RestControllerAdvice` to return consistent error
responses. Every error response contains a `message` and a `timestamp`.

| Exception | HTTP status | What it means |
| --- | --- | --- |
| `TaskNotFoundException` | `404 Not Found` | The requested task ID does not exist. |
| `MethodArgumentNotValidException` | `400 Bad Request` | A request body failed validation, such as a missing title or description. |
| Other unhandled exceptions | `500 Internal Server Error` | An unexpected server error occurred. |

For a missing task, the `message` is `Could not find task with ID: {id}`.

### Create a task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Review pull request","description":"Check the API changes"}'
```

Task responses contain `id`, `title`, `description`, `status`, and `priority`
fields. `status` can be `todo`, `in-progress`, or `done`; `priority` can be
`low`, `medium`, or `high`. New tasks default to `status: "todo"` and
`priority: "low"`.

On create and update requests, explicitly supplied values must match these
lowercase options exactly. Unknown values, uppercase values, surrounding
whitespace, blanks, and `null` return `400 Bad Request` with a validation
message. Omitting these fields keeps the request defaults of `todo` and `low`.

## Database and seed data

The application uses an in-memory H2 database at `jdbc:h2:mem:taskflow-api`. Four sample tasks are loaded each time the application starts, so data is reset on restart.

The H2 console is available at `http://localhost:8080/h2` with:

- JDBC URL: `jdbc:h2:mem:taskflow-api`
- User: `sa`
- Password: empty

## AI summary configuration

The summary endpoint uses Spring AI with the configured OpenAI-compatible base URL and model. Set `SPRING_AI_OPENAI_API_KEY` in the environment before starting the application.

Do not commit API keys to source control. The existing `application.properties` contains a hard-coded key; revoke or rotate it and replace it with an environment-based value before deploying or sharing this project.

## Project layout

```text
src/main/java/sg/edu/ntu/taskflow_api/
├── config/       # Startup seed data
├── controller/   # HTTP endpoints
├── exceptions/   # Task-specific exceptions
├── model/        # JPA entities
├── repository/   # Spring Data repositories
└── service/      # Task business logic
```