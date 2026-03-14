# Notifications Backend Guide

This document describes the notifications implementation in the Spring backend (`dater`) and how mobile push support is wired.

## Scope

Implemented notification types:
- `ATTENDEE_ACCEPTED`
- `CHAT_MESSAGE`

Delivery channels:
- In-app notifications (persisted in database)
- Push notifications (Expo push API, when user has stored push token)

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

## API endpoints

Authenticated endpoints:

- `GET /notifications`
  - Returns latest notifications and unread count for current user.

- `PUT /notifications/read-all`
  - Marks current user's unread notifications as read.

- `PUT /users/push-token`
  - Stores or clears current user's Expo push token.
  - `null` clears token.

## Trigger points

Notifications are created by `NotificationService` from domain events:

- `notifyAttendeeAccepted(...)`
  - Called when date owner accepts a requester.

- `notifyNewChatMessage(...)`
  - Called when a new chat message is sent to the other participant.

Both methods:
1. Persist an in-app notification row.
2. Try to send push through `PushNotificationDeliveryService`.

## Push delivery behavior

`PushNotificationDeliveryService` posts payloads to:
- `https://exp.host/--/api/v2/push/send`

Payload includes:
- `to` (Expo token)
- `title`
- `body`
- `data.dateId`
- `sound`

If token is missing/blank:
- Push is skipped safely.
- In-app notification still exists in DB.

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
- `../dater-mobile/README_DEV_CLIENT_AND_PUSH_SETUP.md`

That guide explains:
- EAS/dev client workflow
- Firebase registration (`google-services.json`)
- Rebuild requirements for native config changes
