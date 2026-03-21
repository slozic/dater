# Notifications Backend Guide

This document describes the notifications implementation in the Spring backend (`dater`) and how mobile push support is wired.

## Scope

Implemented notification types:
- `ATTENDEE_ACCEPTED`
- `DATE_REQUEST_RECEIVED`
- `CHAT_MESSAGE`

Delivery channels:
- In-app notifications (persisted in database)
- Push notifications (Expo push API, when user has stored push token)

Per-user preferences:
- `attendeeAcceptedNotificationsEnabled`
- `dateRequestNotificationsEnabled`
- `chatMessageNotificationsEnabled`

## Data model

Main entity:
- `AppNotification` mapped to `notifications` table

Key fields:
- `id`
- `userId`
- `type`
- `title`
- `body`
- `relatedDateId`
- `createdAt`
- `readAt`

Repository:
- `AppNotificationRepository`
  - read latest notifications
  - unread count
  - mark all unread as read

User model additions:
- `users.push_token`
- notification preference booleans above

## API endpoints

Authenticated endpoints:

- `GET /notifications`
  - Returns latest notifications and unread count for current user.

- `PUT /notifications/read-all`
  - Marks current user's unread notifications as read.

- `PUT /users/push-token`
  - Stores or clears current user's Expo push token.
  - `null` clears token.

- `PUT /users/profile`
  - Updates user profile fields, including notification preferences.

## Trigger points

Notifications are created by `NotificationService` from domain events:

- `notifyAttendeeAccepted(...)`
  - Called when date owner accepts a requester.

- `notifyDateRequestReceived(...)`
  - Called when a user requests to join a date (target: date owner).

- `notifyNewChatMessage(...)`
  - Called when a new chat message is sent to the other participant.

Each path:
1. checks per-type user preference
2. persists an in-app notification
3. attempts push dispatch with deep-link metadata (`dateId`, `notificationType`)

## Push delivery behavior

`PushNotificationDeliveryService` posts payloads to:
- `https://exp.host/--/api/v2/push/send`

Payload includes:
- `to` (Expo token)
- `title`
- `body`
- `data.dateId`
- `data.notificationType`
- `sound`

If token is missing/blank:
- Push is skipped safely.
- In-app notification still exists in DB.

Core attendee/chat actions are wrapped to remain resilient even if notification dispatch fails.

## Logging and troubleshooting

Useful log lines:

- Token update:
  - `Updated push token for user ... tokenPresent=... tokenSuffix=...`
- Push skipped:
  - `Push skipped for user ...: no push token stored.`
- Push attempted:
  - `Push send attempted. status=... tokenSuffix=... response=...`

Typical diagnosis flow:
1. Confirm `PUT /users/push-token` is called after mobile login.
2. Confirm `tokenPresent=true` appears in logs.
3. Trigger notification event.
4. Confirm push attempt log appears.

## Mobile-side setup reference

Mobile Expo + Firebase setup is documented in:
- `../../dater-mobile/docs/README_DEV_CLIENT_AND_PUSH_SETUP.md`
