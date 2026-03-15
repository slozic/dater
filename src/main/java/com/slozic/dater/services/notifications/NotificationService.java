package com.slozic.dater.services.notifications;

import com.slozic.dater.dto.response.notifications.NotificationDto;
import com.slozic.dater.dto.response.notifications.NotificationListResponse;
import com.slozic.dater.models.AppNotification;
import com.slozic.dater.models.NotificationType;
import com.slozic.dater.models.User;
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
        createAndSendIfEnabled(attendeeUserId, NotificationType.ATTENDEE_ACCEPTED, title, body, dateId);
    }

    @Transactional
    public void notifyDateRequestReceived(
            final UUID ownerUserId,
            final UUID dateId,
            final String dateTitle,
            final String requesterUsername
    ) {
        final String title = "New date request";
        final String body = requesterUsername + " requested to join your date \"" + dateTitle + "\".";
        createAndSendIfEnabled(ownerUserId, NotificationType.DATE_REQUEST_RECEIVED, title, body, dateId);
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
        createAndSendIfEnabled(recipientUserId, NotificationType.CHAT_MESSAGE, title, body, dateId);
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

    private void createAndSendIfEnabled(
            final UUID userId,
            final NotificationType type,
            final String title,
            final String body,
            final UUID dateId
    ) {
        userRepository.findOneById(userId).ifPresentOrElse(
                user -> {
                    if (!isNotificationEnabled(user, type)) {
                        log.debug("Notification skipped for user {}: {} disabled.", userId, type);
                        return;
                    }
                    appNotificationRepository.save(AppNotification.builder()
                            .userId(userId)
                            .type(type)
                            .title(title)
                            .body(body)
                            .relatedDateId(dateId)
                            .build());
                    sendPushIfEnabled(user, type, title, body, dateId);
                },
                () -> log.warn("Push skipped: user {} not found.", userId)
        );
    }

    private void sendPushIfEnabled(
            final User user,
            final NotificationType type,
            final String title,
            final String body,
            final UUID dateId
    ) {
        final String pushToken = user.getPushToken();
        if (pushToken == null || pushToken.isBlank()) {
            log.debug("Push skipped for user {}: no push token stored.", user.getId());
            return;
        }
        pushNotificationDeliveryService.sendPush(pushToken, title, body, dateId.toString(), type.name());
    }

    private boolean isNotificationEnabled(final User user, final NotificationType type) {
        return switch (type) {
            case ATTENDEE_ACCEPTED -> user.isAttendeeAcceptedNotificationsEnabled();
            case DATE_REQUEST_RECEIVED -> user.isDateRequestNotificationsEnabled();
            case CHAT_MESSAGE -> user.isChatMessageNotificationsEnabled();
        };
    }
}
