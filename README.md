# reminder-api

A small Spring Boot service for reminders and a month calendar view. It exists to
be deployed: it is deliberately boring so that when the delivery pipeline goes
red, the application is not the suspect.

- Java 17, Spring Boot 4.1.1, Maven
- Storage is in-memory and seeded at startup. Swapping in Postgres touches only
  `domain/` — see `ReminderRepository`.
- Actuator exposes the two probes Kubernetes wants.

## Run it

    mvn -B clean package
    java -jar target/reminder-api-0.0.1-SNAPSHOT.jar

    docker build -t reminder-api:local .
    docker run --rm -p 8080:8080 reminder-api:local

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/reminders` | Optional `?done=`, `?from=`, `?to=` (dates as `yyyy-MM-dd`) |
| GET | `/api/reminders/upcoming?days=7` | Open reminders in the next N days |
| GET | `/api/reminders/{id}` | 404 if unknown |
| POST | `/api/reminders` | 201 with a `Location` header |
| PUT | `/api/reminders/{id}` | Replace; keeps `done` and `createdAt` |
| PUT | `/api/reminders/{id}/done` | Mark complete |
| DELETE | `/api/reminders/{id}/done` | Reopen |
| DELETE | `/api/reminders/{id}` | 204 |
| GET | `/api/calendar/{year}/{month}` | Sparse month view grouped by day |
| GET | `/actuator/health/readiness` | Readiness probe |
| GET | `/actuator/health/liveness` | Liveness probe |

Omit `dueTime` for an all-day reminder.

    curl -X POST http://localhost:8080/api/reminders \
      -H "Content-Type: application/json" \
      -d '{"title":"Renew passport","dueDate":"2026-11-03","dueTime":"09:30"}'

## Notes for the delivery pipeline

- `server.shutdown=graceful` so rolling updates do not drop in-flight requests.
- The image runs as a non-root user; some clusters reject root containers.
- `MaxRAMPercentage=75` makes the JVM respect the container memory limit rather
  than the host's.
- State is in memory, so replicas do not share data. Keep `replicaCount: 1`
  until the Postgres swap.
