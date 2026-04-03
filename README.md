# Dater Backend (`dater`)

Spring Boot backend for a date-first social app. The focus is creating real date events, requesting to join, accepting one attendee, and continuing conversation through a scoped date chat.

## Stack

- Java 25
- Spring Boot 4
- Spring Security + JWT (access + refresh tokens)
- PostgreSQL + Flyway migrations
- JPA/Hibernate
- JUnit + MockMvc + Testcontainers

## Platform baseline

- The backend is now aligned on a modern platform baseline (Java 25 + Spring Boot 4).
- Build and test workflows are validated on this baseline for ongoing backend development.

## Java setup with mise (Windows)

`mise` is the recommended way to manage local Java versions for this project.

1. Install `mise` with WinGet:
   - `winget install jdx.mise`
2. Add shell activation to your PowerShell profile (PowerShell 7+ recommended):
   - `if (!(Test-Path $PROFILE)) { New-Item -ItemType File -Path $PROFILE -Force | Out-Null }`
   - `Add-Content $PROFILE '(& mise activate pwsh) | Out-String | Invoke-Expression'`
3. Ensure shims are available in all terminals (including IDE terminals):
   - Add `%USERPROFILE%\.local\share\mise\shims` to your User `PATH`.
   - Add `%LOCALAPPDATA%\Microsoft\WinGet\Links` to User `PATH` if `mise` is not found.
4. Restart terminals/IDEs and set Java:
   - `mise use -g java@25`
   - `mise reshim`

Useful commands:
- Verify selected Java:
  - `mise which java`
  - `java -version`
- Switch versions quickly:
  - `mise use -g java@21`
  - `mise use -g java@25`
- Pin a project-specific version (from repo root):
  - `mise use java@25`

Windows + IDE troubleshooting:
- If `mise which java` shows Java 25 but `java -version` still shows an older JDK, your shell `PATH` order is overriding `mise`.
- Remove old global Java paths (`JAVA_HOME`/`...\Java\...\bin`) or move them after `mise` shims.
- In IntelliJ terminal, use `pwsh.exe` and avoid `-NoProfile` so profile activation runs.
- Fully restart IntelliJ/VS Code after `PATH` or profile changes.

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
  - `./mvnw clean verify`
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
