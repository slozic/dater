package com.slozic.dater.services.notifications;

import com.slozic.dater.dto.response.notifications.NotificationDto;
import com.slozic.dater.dto.response.notifications.NotificationListResponse;
import com.slozic.dater.models.AppNotification;
import com.slozic.dater.models.NotificationType;
import com.slozic.dater.repositories.AppNotificationRepository;
import com.slozic.dater.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final AppNotificationRepository appNotificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationDeliveryService pushNotificationDeliveryService;

    @Transactional
    public void notifyAttendeeAccepted(final UUID attendeeUserId, final UUID dateId, final String dateTitle) {
        final String title = "Request accepted";
        final String body = "Your request for \"" + dateTitle + "\" has been accepted.";
        appNotificationRepository.save(AppNotification.builder()
                .userId(attendeeUserId)
                .type(NotificationType.ATTENDEE_ACCEPTED)
                .title(title)
                .body(body)
                .relatedDateId(dateId)
                .build());
        sendPushIfEnabled(attendeeUserId, title, body, dateId);
    }

    @Transactional
    public void notifyNewChatMessage(
            final UUID recipientUserId,
            final UUID dateId,
            final String dateTitle,
            final String senderUsername
    ) {
        final String title = "New message";
        final String body = senderUsername + " sent you a message about \"" + dateTitle + "\".";
        appNotificationRepository.save(AppNotification.builder()
                .userId(recipientUserId)
                .type(NotificationType.CHAT_MESSAGE)
                .title(title)
                .body(body)
                .relatedDateId(dateId)
                .build());
        sendPushIfEnabled(recipientUserId, title, body, dateId);
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getUserNotifications(final UUID userId) {
        final List<NotificationDto> notifications = appNotificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::from)
                .toList();
        final long unreadCount = appNotificationRepository.countByUserIdAndReadAtIsNull(userId);
        return new NotificationListResponse(unreadCount, notifications);
    }

    @Transactional
    public void markAllAsRead(final UUID userId) {
        appNotificationRepository.markAllAsRead(userId, OffsetDateTime.now());
    }

    private void sendPushIfEnabled(final UUID userId, final String title, final String body, final UUID dateId) {
        userRepository.findOneById(userId).ifPresentOrElse(
                user -> {
                    final String pushToken = user.getPushToken();
                    if (pushToken == null || pushToken.isBlank()) {
                        log.info("Push skipped for user {}: no push token stored.", userId);
                        return;
                    }
                    pushNotificationDeliveryService.sendPush(pushToken, title, body, dateId.toString());
                },
                () -> log.warn("Push skipped: user {} not found.", userId)
        );
    }
}
