# Dater Project Notes

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

## Web Frontend (dater-frontend)
- Date list uses optional geo filter; UI simplified to radius + “Use my location”.
- Date creation uses “Use my location” to store lat/lng (no manual lat/lng fields).
- Profile page supports editing (calls `PUT /users/profile`).
- Image display switched to `imageUrl` for profile/date images.

## Mobile (dater-mobile / Expo)
- Created Expo app at `C:\Users\sly-x\projects\spring\dater-mobile`.
- Added API helper with SecureStore token storage and basic endpoints.
- Implemented mobile Login + Dates list + Date details + Date creation + Profile (edit).
- Added modern styling: light background, cards, accent buttons.
- Tab bar hidden when logged out; added My Dates tab (created/requested) and excluded own/requested from main list.
- Dates list now supports radius filtering with location opt-in and shows empty-state hints.
- Unified accent styles + press feedback for buttons and cards; softened date card tint.
- Added shared auth context to keep tab bar state in sync after login/logout.
- Added create-date required field validation with inline errors (title, location, description, date/time).
- Added registration required field validation on mobile (including gender/birthday).
- Added login form validation (required username/password) with generic auth failure messaging.
- Added Settings section on profile with Logout (clears token).
- Note: Expo expects Node 20+; Node upgraded to v24.13.0.
- Replaced deprecated SafeAreaView with `react-native-safe-area-context`.
- Added mobile registration screen (`/auth/register`) and login button to open it.
- Updated image picker usage to `ImagePicker.MediaType.Images`.
- Profile now refreshes on focus to show current logged-in user.
- Date details only show image upload for the date owner; join requests can be canceled (ON_WAITLIST).
- Added auth expiry handling: on 401 responses, token is cleared and app returns to login.
- Added automatic access-token refresh and retry-once behavior using stored refresh tokens.
- Profile page reordered to: photos first, details second, settings last.
- Profile details now display `Full name`; birthday/gender are visible but read-only in edit mode.
- Profile/date CTA buttons are now compact, centered, and more visually consistent.
- Date details requests are hidden by default behind a "View requests" toggle and use cleaner card-style rows.
- Date details now has explicit creator profile navigation via `View profile` button.
- Added date chat UI:
  - New chat screen `app/date/chat/[id].tsx` with polling-based updates.
  - New `Open chat` action in date details (available to owner or accepted attendee).
  - Added chat API client methods in `lib/api.ts`.

## Completed Mobile Port Tasks
- Date images (view/upload/delete) in Date Details.
- Date request/accept flow (join + owner accept/reject) in Date Details.
- Profile images (view/upload/delete) in Profile.
- Request list UX: accepted highlight + waitlist toggle.
- Request list: accept/reject feedback messages + profile link.
- Join status hidden for owner.
- Date creation updated with GPS lookup, date/time picker, and image uploads.
- Date edit and delete flows added for owner in Date Details.
- Image zoom/full-screen viewer added for date images and profile/public-profile images.
- Gender added end-to-end (backend model + migration, mobile registration, profile and public profile display).
- Profile Settings now includes "Show dates from" selector (All/Male/Female).
- My Dates now has on-demand "View past dates" section with muted/strikethrough styling for expired items.
- My Dates keeps past section expanded when returning from date details.
- Past date details keep owner delete only (no edit), and disable active interactions (join/waitlist/upload).
- Main Dates cards now display human-readable date/time (aligned with My Dates cards).

## Pending Mobile Port Tasks
- Report / block users.
- General UI polish (header title instead of `date/[id]`, spacing, alignment).

## Location UX Options (evaluated)
- Manual entry only (current): fastest, no API keys or billing.
- Map picker with pin + reverse geocode: needs map SDK + billing; may return address more reliably than POI name.
- Search/autocomplete (Mapbox/Google): best UX, requires API key + billing; Google has richer POI coverage, Mapbox simpler/cheaper for hobby scale.

## Date Creation Location Toggle (follow-up ideas)
- Persist the toggle choice per user.
- Make it a soft opt-in on first use.
- Add a brief privacy reminder in Settings.
