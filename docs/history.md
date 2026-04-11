# Project History Archive

This file preserves the previous long-form project context that used to live in `agents.md`.
It is intentionally kept as a historical running log for future reference.

# Dater Project Notes

## 2026-03 Platform Upgrade Summary (Backend)
- Established Java 25 and Spring Boot 4 as the backend platform baseline for ongoing development.
- Aligned backend configuration and dependency management so framework-level changes are easier to maintain.
- Adapted security and test infrastructure to the newer framework behavior without changing core product flows.
- Stabilized startup, integration-test bootstrap, and full verify execution on the new platform baseline.
- Refreshed build-tooling alignment for modern Java compatibility and more consistent local/CI behavior.

## 2026-04 Moderation + migration visibility follow-up (Backend)
- Added user moderation schema migration `V18__add_user_reports_and_blocks.sql` with:
  - `user_blocks` (directional blocker/blocked relationship, uniqueness, indexes),
  - `user_reports` (reason + optional note, indexes).
- Added moderation domain/API:
  - `POST /users/{id}/moderation/report`
  - `POST /users/{id}/moderation/block`
  - `POST /users/{id}/moderation/report-and-block`
- Enforced strict block behavior across existing interaction flows:
  - public profile access,
  - date visibility/list-detail checks,
  - attendee request/accept/reject paths,
  - chat read/send access.
- Added dedicated blocked-interaction handling with clear `403` error responses.
- Added integration coverage for moderation and strict-block enforcement (`UserModerationControllerIT`).
- Fixed startup migration visibility gap by explicitly adding `spring-boot-flyway`; Flyway now runs and logs schema state at backend startup.
- Switched logging config to `logback-spring.xml` so profile-specific logging (`<springProfile>`) is applied correctly.

## Backend (dater)
- Added geo fields to `dates` (latitude/longitude) and optional radius filtering via `GET /dates?latitude=...&longitude=...&radiusKm=...`.
- Migrated `date_attendees` from `accepted/soft_deleted` booleans to `status` enum with new migration `V9__add_geo_and_attendee_status.sql`.
- Added `PUT /dates/{id}` for date updates.
- Added `PUT /users/profile` for profile updates (first/last/username/birthday/gender).
- Image responses now return URLs instead of base64; new media paths served at `/media/user/**` and `/media/date/**`.
- Security update: `/media/**` is publicly accessible (required for image loading).
- Added cancel join request endpoint: `DELETE /dates/{id}/attendees/me` (only when status is ON_WAITLIST).
- Requested dates now exclude `REJECTED`; main list hides dates rejected by the current user.
- Added users gender field (`V10__add_gender_to_users.sql`) and exposed it in profile/public-profile responses.
- Hardened registration validation (`@Valid` + field constraints) to return 400 instead of 500 on missing fields.
- Added user discovery preference (`date_list_gender_filter`) and applied it to main date list results (all/male/female).
- Date list API now defaults to upcoming-only (`now + 1 min`); supports `includePast=true` for historical fetches.
- Added refresh-token auth flow:
  - Login now returns both `Authorization` (access) and `Refresh-Token`.
  - New `POST /auth/refresh` endpoint rotates tokens.
  - JWTs now carry token type (`ACCESS`/`REFRESH`) and are validated by usage.
  - CORS exposes `Refresh-Token` header for clients.
- Added date chat backend (owner <-> accepted attendee):
  - New `chat_messages` storage (`V12__add_chat_messages.sql`).
  - New endpoints:
    - `GET /dates/{id}/chat/messages`
    - `POST /dates/{id}/chat/messages`
  - Access control enforces date owner + currently accepted attendee only.
- Added/updated backend tests for refreshed functionality:
  - New unit tests for JWT token-type validation and chat service authorization.
  - New integration tests for `POST /auth/refresh` and date chat endpoints.
  - Updated legacy integration fixtures/tests for attendee `status` model and `includePast` defaults.
  - Testcontainers dependency updated for local Docker Desktop compatibility.
- Chat thread isolation update:
  - New migration `V13__scope_chat_messages_by_thread.sql` to scope messages by active accepted attendee thread.
  - Chat reads/writes are now thread-scoped (`date_id + participant_user_id`) to avoid showing previous accepted-user conversations.
- Added modular in-app notifications backend:
  - New `notifications` table (`V14__add_notifications.sql`) and endpoints `GET /notifications`, `PUT /notifications/read-all`.
  - Notifications are generated for `ATTENDEE_ACCEPTED` and `CHAT_MESSAGE` events.
- Added push token support for mobile push delivery:
  - Added `users.push_token` (`V15__add_push_token_to_users.sql`).
  - New endpoint `PUT /users/push-token`.
  - Notification service now sends Expo push requests when a recipient has a push token.
- Added backend notification/push troubleshooting and setup docs:
  - `README_NOTIFICATIONS_BACKEND.md` (backend notification model, endpoints, delivery flow, logs).
  - `README.md` now links to backend/mobile notification setup docs.
- Notification preference + resiliency backend follow-ups:
  - Added `DATE_REQUEST_RECEIVED` notification type and owner notification trigger on new attendee requests.
  - Added per-user notification preference fields + migration (`V16__add_notification_preferences_to_users.sql`).
  - Notification creation/sending now respects per-type preferences.
  - Push payload now includes `notificationType` for mobile deep-link routing.
  - Hardened attendee/chat flows to avoid failing core actions when notification dispatch fails.
  - Expanded unit/integration tests for notification preferences, date-request notification creation, and attendee-accepted notification paths.

## Web Frontend (dater-frontend)
- Date list uses optional geo filter; UI simplified to radius + "Use my location".
- Date creation uses "Use my location" to store lat/lng (no manual lat/lng fields).
- Profile page supports editing (calls `PUT /users/profile`).
- Image display switched to `imageUrl` for profile/date images.

## Mobile (dater-mobile / Expo)
- Mobile change history has been moved to:
  - `C:\Users\sly-x\projects\spring\dater-mobile\docs\history.md`
- Backend history remains backend-focused in this file.

## 2026-03-21 Full Code Review (Backend) Follow-up
- Context:
  - This section captures backend hardening actions implemented after a strict full-project review.
  - The goal was to close high-confidence security/correctness issues and keep a checklist reference for future sessions.
- Status checklist:
  - `[x] C1` Enforced date-owner authorization for attendee moderation (`accept`/`reject`) in `DateAttendeesService`.
  - `[x] C2` Enforced profile-image ownership on delete in `ProfileImageService`.
  - `[x] C3` Added username uniqueness checks in registration and DB-level uniqueness via `V17__add_unique_constraint_to_users_username.sql`.
  - `[x] H2` Added dedicated `404` handling for `DateEventNotFoundException`.
  - `[x] H3` Added explicit exception mappings (`AttendeeAlreadyExistsException -> 409`, `IllegalArgumentException -> 400`, `DateTimeException -> 400`, profile-image access -> `403`).
  - `[x] H5` Switched push delivery to shared injected `HttpClient` bean.
  - `[x] H6` Added DTO/controller validation for date create/update and trim/sanitize handling in service layer.
  - `[x] M1` Profile-image delete now removes both storage file and DB record.
  - `[x] M2` Refactored `ProfileImageService` to constructor injection.
  - `[x] M3` Removed unused auth DAO implementations (`DefaultApplicationUserDaoImpl`, `H2ApplicationUserDaoImpl`) and deleted dead helper in postgres DAO.
  - `[x] M4` Clarified auth DAO contract naming (`selectApplicationUserByEmail`) and aligned call sites/messages.
  - `[x] L2` Removed commented-out dead code in `JWTUtils`.
  - `[x] L3` Removed unnecessary `Serializable` from `JwtAuthenticationEntryPoint`.
  - `[x] L7` Added `equals`/`hashCode` to `DateAttendee` based on embedded id.
  - `[~] H1` Config moved to env-overridable values (`DB_*`, `JWT_SIGNING_KEY`) with local defaults preserved.
  - `[ ] H4` Early legacy migration seed cleanup intentionally deferred to avoid rewriting historical migration behavior.
  - `[ ] M5` `DateEventResponse` legacy empty fields left unchanged.
  - `[ ] M6` Notification inbox API still present (not deprecated/removed in this pass).
  - `[ ] M7` No pagination introduced yet for date/chat list endpoints.
  - `[ ] M8` Legacy `data.sql` retained as-is.
  - `[ ] M9` Collection style consistency pass deferred.
  - `[ ] L1` Entity rename `Date` deferred.
  - `[ ] L4` Logging aspect broadening/refactor deferred.
  - `[ ] L5` OpenAPI placeholder metadata unchanged.
  - `[ ] L6` No additional `ErrorResponse` refactor applied.
- Verification:
  - Targeted suite run: `DateAttendeeServiceTest`, `UserServiceTest`, `DateAttendeeControllerIT`, `DateEventControllerIT`, `ProfileImageControllerIT`.
  - Result: `35 tests`, `0 failures`, `0 errors` (BUILD SUCCESS).
  - Post-verify follow-up:
    - `mvn clean verify` exposed a DI mismatch (`UserService` required `BCryptPasswordEncoder` while config exposed `PasswordEncoder` bean type).
    - Fixed by changing `UserService` (and corresponding unit test mock) to depend on `PasswordEncoder` interface.
    - Re-validated with `DateAttendeeControllerIT` + `UserServiceTest` (BUILD SUCCESS).

## Location UX Options (evaluated)
- Manual entry only (current): fastest, no API keys or billing.
- Map picker with pin + reverse geocode: needs map SDK + billing; may return address more reliably than POI name.
- Search/autocomplete (Mapbox/Google): best UX, requires API key + billing; Google has richer POI coverage, Mapbox simpler/cheaper for hobby scale.

## Date Creation Location Toggle (follow-up ideas)
- Persist the toggle choice per user.
- Make it a soft opt-in on first use.
- Add a brief privacy reminder in Settings.
