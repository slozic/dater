# Dater Project Notes

## Backend (dater)
- Added geo fields to `dates` (latitude/longitude) and optional radius filtering via `GET /dates?latitude=...&longitude=...&radiusKm=...`.
- Migrated `date_attendees` from `accepted/soft_deleted` booleans to `status` enum with new migration `V9__add_geo_and_attendee_status.sql`.
- Added `PUT /dates/{id}` for date updates.
- Added `PUT /users/profile` for profile updates (first/last/username/birthday).
- Image responses now return URLs instead of base64; new media paths served at `/media/user/**` and `/media/date/**`.
- Security update: `/media/**` is publicly accessible (required for image loading).
- Added cancel join request endpoint: `DELETE /dates/{id}/attendees/me` (only when status is ON_WAITLIST).

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
- Added Settings section on profile with Logout (clears token).
- Note: Expo expects Node 20+; Node upgraded to v24.13.0.
- Replaced deprecated SafeAreaView with `react-native-safe-area-context`.
- Added mobile registration screen (`/auth/register`) and login button to open it.
- Updated image picker usage to `ImagePicker.MediaType.Images`.
- Profile now refreshes on focus to show current logged-in user.
- Date details only show image upload for the date owner; join requests can be canceled (ON_WAITLIST).
- Added auth expiry handling: on 401 responses, token is cleared and app returns to login.

## Completed Mobile Port Tasks
- Date images (view/upload/delete) in Date Details.
- Date request/accept flow (join + owner accept/reject) in Date Details.
- Profile images (view/upload/delete) in Profile.
- Request list UX: accepted highlight + waitlist toggle.
- Request list: accept/reject feedback messages + profile link.
- Join status hidden for owner.

## Pending Mobile Port Tasks
- Date creation: add image upload and map/geo lookup for typed location.
- Token refresh / persistent login (refresh tokens or longer JWT expiry).
- Accept/reject UX feedback (show status update and hide processed requests).
- Add chat option (post-accept).
- Report / block users.
- Image zoom / full-screen viewer for date + profile images.
- Add radius filter UI in mobile date list.
- Date display formatting (human-readable date/time).
- Join status presentation (hide for owner; show for requester).
- Link to user profile from date details.
- General UI polish (header title instead of `date/[id]`, spacing, alignment).
