# Dater Backend (`dater`)

Spring Boot backend for a date-first social app. The focus is creating real date events, requesting to join, accepting one attendee, and continuing conversation through a scoped date chat.

## Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT (access + refresh tokens)
- PostgreSQL + Flyway migrations
- JPA/Hibernate
- JUnit + MockMvc + Testcontainers

## Core capabilities

- User registration/login and token refresh
- Date CRUD + geolocation fields and optional radius filtering
- Date attendee flow:
  - request to join
  - owner accept/reject
  - requester cancel while waitlisted
- Thread-scoped date chat between owner and accepted attendee
- Notification system:
  - in-app notifications
  - Expo push notifications (when push token exists)
  - per-type user notification preferences
  - notification deep-link metadata (`notificationType`, `dateId`)

## Local setup

1. Start database:
   - `docker compose up -d`
2. Run backend:
   - `./mvnw spring-boot:run`
3. API base URL:
   - `http://localhost:8080`

## Tests

- Full test suite:
  - `./mvnw test`
- Integration tests run with Testcontainers.

## Project docs

- Backend notifications and push internals:
  - `docs/README_NOTIFICATIONS_BACKEND.md`
- Historical implementation log (archived from previous `agents.md`):
  - `docs/history.md`
- Mobile dev-client + Firebase push setup (sibling repo):
  - `../dater-mobile/docs/README_DEV_CLIENT_AND_PUSH_SETUP.md`
- Agent guidance and coding conventions:
  - `agents.md`

## Notes

- Context persistence for Cursor is maintained in `.mdc` files:
  - `.cursor/rules/project-context.mdc`
