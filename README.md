# Task Manager API

A Spring Boot 4 Task Management REST API demonstrating 30 essential Spring annotations.

## Prerequisites

- **Java 25** (or later)
- **Maven 3.9+** (or use the included Maven wrapper `./mvnw`)
- **Docker** and **Docker Compose** (PostgreSQL runs via Docker Compose)

## How to Run the Application

Spring Boot Docker Compose support will automatically start the PostgreSQL container defined in `compose.yaml` when the application starts.

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

To stop the application, press `Ctrl+C`. The PostgreSQL container will be stopped automatically (`spring.docker.compose.stop.command=down`).

## How to Run Tests

Tests use Testcontainers to spin up an isolated PostgreSQL instance, so Docker must be running.

```bash
./mvnw test
```

## API Endpoints

Base URL: `http://localhost:8080/api/tasks`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks` | List all tasks |
| GET | `/api/tasks?priority=HIGH` | List tasks filtered by priority |
| GET | `/api/tasks/{id}` | Get a task by ID |
| PUT | `/api/tasks/{id}` | Update a task |
| PATCH | `/api/tasks/{id}/complete` | Mark a task as completed |
| DELETE | `/api/tasks/{id}` | Delete a task |
| GET | `/api/tasks/pending` | List all pending tasks |

## Curl Examples

> Before creating tasks, you need a category in the database. You can insert one manually or via a database client:
>
> ```bash
> docker exec -it $(docker ps -qf "ancestor=postgres:17") \
>   psql -U postgres -d taskdb -c \
>   "INSERT INTO categories (name, description) VALUES ('Work', 'Work tasks');"
> ```

### Create a Task

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish quarterly report",
    "description": "Complete the Q4 financial report",
    "priority": "HIGH",
    "categoryId": 1
  }' | jq
```

### List All Tasks

```bash
curl -s http://localhost:8080/api/tasks | jq
```

### List Tasks by Priority

```bash
curl -s "http://localhost:8080/api/tasks?priority=HIGH" | jq
```

### Get a Task by ID

```bash
curl -s http://localhost:8080/api/tasks/1 | jq
```

### Update a Task

```bash
curl -s -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish quarterly report (updated)",
    "description": "Complete the Q4 financial report with appendix",
    "priority": "CRITICAL"
  }' | jq
```

### Mark a Task as Completed

```bash
curl -s -X PATCH http://localhost:8080/api/tasks/1/complete | jq
```

### Delete a Task

```bash
curl -s -X DELETE http://localhost:8080/api/tasks/1 -w "\nHTTP Status: %{http_code}\n"
```

### List Pending Tasks

```bash
curl -s http://localhost:8080/api/tasks/pending | jq
```

### Validation Error Example

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "priority": null,
    "categoryId": null
  }' | jq
```

### Non-Existent Task (404)

```bash
curl -s http://localhost:8080/api/tasks/999 | jq
```

## Spring + Keycloak + JWT
```bash
TOKEN=$(curl -s -X POST "http://localhost:9090/realms/taskapi/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=task-api-client" \
  -d "username=alice" \
  -d "password=alice123" | jq -r '.access_token')
```

Now let's call the API with this token:

```bash
# Create a task -- should work (alice has USER role)
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn OAuth2","description":"Watch this video","priority":"HIGH","categoryId":1}' | jq
```

Works. 201 Created.

```bash
# Get all tasks -- should work
curl -s http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" | jq
```

Works. Let's try to delete -- alice is NOT an admin:

```bash
# Delete task -- should fail (alice has no ADMIN role)
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

`403 Forbidden`. Exactly what we expected. Alice can't delete tasks.

Now let's get a token for `bob` -- he has both `user` and `admin` roles:

```bash
TOKEN=$(curl -s -X POST "http://localhost:9090/realms/taskapi/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=task-api-client" \
  -d "username=bob" \
  -d "password=bob123" | jq -r '.access_token')

# Delete task -- should work (bob has ADMIN role)
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

`204 No Content`. Bob can delete. Role-based access control is working.

And just to be complete -- what happens without a token?

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/tasks
```

`401 Unauthorized`. The API is fully locked down.
