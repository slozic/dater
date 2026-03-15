package com.slozic.dater.services;

import com.slozic.dater.models.NotificationType;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.AppNotificationRepository;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.services.notifications.NotificationService;
import com.slozic.dater.services.notifications.PushNotificationDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private AppNotificationRepository appNotificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PushNotificationDeliveryService pushNotificationDeliveryService;

    @Test
    void notifyAttendeeAccepted_shouldPersistAndSendWhenEnabled() {
        final UUID userId = UUID.randomUUID();
        final UUID dateId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .username("guest")
                .attendeeAcceptedNotificationsEnabled(true)
                .pushToken("ExponentPushToken[abc12345]")
                .build();
        when(userRepository.findOneById(userId)).thenReturn(Optional.of(user));

        notificationService.notifyAttendeeAccepted(userId, dateId, "Pool date");

        final ArgumentCaptor<com.slozic.dater.models.AppNotification> captor =
                ArgumentCaptor.forClass(com.slozic.dater.models.AppNotification.class);
        verify(appNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ATTENDEE_ACCEPTED);
        assertThat(captor.getValue().getRelatedDateId()).isEqualTo(dateId);
        verify(pushNotificationDeliveryService).sendPush(
                eq("ExponentPushToken[abc12345]"),
                eq("Request accepted"),
                eq("Your request for \"Pool date\" has been accepted."),
                eq(dateId.toString()),
                eq("ATTENDEE_ACCEPTED")
        );
    }

    @Test
    void notifyAttendeeAccepted_shouldSkipWhenDisabled() {
        final UUID userId = UUID.randomUUID();
        final UUID dateId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .username("guest")
                .attendeeAcceptedNotificationsEnabled(false)
                .pushToken("ExponentPushToken[abc12345]")
                .build();
        when(userRepository.findOneById(userId)).thenReturn(Optional.of(user));

        notificationService.notifyAttendeeAccepted(userId, dateId, "Pool date");

        verify(appNotificationRepository, never()).save(any());
        verify(pushNotificationDeliveryService, never()).sendPush(any(), any(), any(), any(), any());
    }

    @Test
    void notifyDateRequestReceived_shouldPersistAndSendWhenEnabled() {
        final UUID userId = UUID.randomUUID();
        final UUID dateId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .username("owner")
                .dateRequestNotificationsEnabled(true)
                .pushToken("ExponentPushToken[abc12345]")
                .build();
        when(userRepository.findOneById(userId)).thenReturn(Optional.of(user));

        notificationService.notifyDateRequestReceived(userId, dateId, "Pool date", "guest");

        final ArgumentCaptor<com.slozic.dater.models.AppNotification> captor =
                ArgumentCaptor.forClass(com.slozic.dater.models.AppNotification.class);
        verify(appNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.DATE_REQUEST_RECEIVED);
        assertThat(captor.getValue().getRelatedDateId()).isEqualTo(dateId);
        verify(pushNotificationDeliveryService).sendPush(
                eq("ExponentPushToken[abc12345]"),
                eq("New date request"),
                eq("guest requested to join your date \"Pool date\"."),
                eq(dateId.toString()),
                eq("DATE_REQUEST_RECEIVED")
        );
    }

    @Test
    void notifyNewChatMessage_shouldSkipWhenChatNotificationsDisabled() {
        final UUID userId = UUID.randomUUID();
        final UUID dateId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .username("guest")
                .chatMessageNotificationsEnabled(false)
                .pushToken("ExponentPushToken[abc12345]")
                .build();
        when(userRepository.findOneById(userId)).thenReturn(Optional.of(user));

        notificationService.notifyNewChatMessage(userId, dateId, "Pool date", "owner");

        verify(appNotificationRepository, never()).save(any());
        verify(pushNotificationDeliveryService, never()).sendPush(any(), any(), any(), any(), any());
    }
}
