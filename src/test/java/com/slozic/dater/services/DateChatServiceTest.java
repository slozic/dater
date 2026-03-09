package com.slozic.dater.services;

import com.slozic.dater.dto.response.chat.ChatMessageDto;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.models.ChatMessage;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.ChatMessageRepository;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.services.chat.DateChatService;
import com.slozic.dater.services.notifications.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.slozic.dater.dto.enums.JoinDateStatus.ACCEPTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DateChatServiceTest {
    @InjectMocks
    private DateChatService dateChatService;

    @Mock
    private DateEventRepository dateEventRepository;
    @Mock
    private DateAttendeeRepository dateAttendeeRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @Test
    void sendMessage_shouldRouteOwnerMessageToAcceptedAttendee() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).title("Pool date").build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));
        when(userRepository.findOneById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).username("owner").build()));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            final ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        final ChatMessageDto dto = dateChatService.sendMessage(dateId.toString(), ownerId, "hello");

        assertThat(dto.senderId()).isEqualTo(ownerId.toString());
        assertThat(dto.recipientId()).isEqualTo(acceptedAttendeeId.toString());
        assertThat(dto.message()).isEqualTo("hello");
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(notificationService).notifyNewChatMessage(acceptedAttendeeId, dateId, "Pool date", "owner");
    }

    @Test
    void sendMessage_shouldRouteAcceptedAttendeeMessageToOwner() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).title("Pool date").build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));
        when(userRepository.findOneById(acceptedAttendeeId))
                .thenReturn(Optional.of(User.builder().id(acceptedAttendeeId).username("guest").build()));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            final ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        final ChatMessageDto dto = dateChatService.sendMessage(dateId.toString(), acceptedAttendeeId, "hi owner");

        assertThat(dto.senderId()).isEqualTo(acceptedAttendeeId.toString());
        assertThat(dto.recipientId()).isEqualTo(ownerId.toString());
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(notificationService).notifyNewChatMessage(ownerId, dateId, "Pool date", "guest");
    }

    @Test
    void getMessagesForDate_shouldRejectNonParticipants() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final UUID randomUser = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).title("Pool date").build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));

        assertThrows(
                DateEventAccessPermissionException.class,
                () -> dateChatService.getMessagesForDate(dateId.toString(), randomUser)
        );
    }

    @Test
    void getMessagesForDate_shouldReadOnlyCurrentThreadMessages() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).title("Pool date").build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();

        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));
        when(chatMessageRepository.findAllByDateIdAndParticipantUserIdOrderByCreatedAtAsc(dateId, acceptedAttendeeId))
                .thenReturn(List.of());

        dateChatService.getMessagesForDate(dateId.toString(), ownerId);

        verify(chatMessageRepository).findAllByDateIdAndParticipantUserIdOrderByCreatedAtAsc(
                eq(dateId),
                eq(acceptedAttendeeId)
        );
    }
}
