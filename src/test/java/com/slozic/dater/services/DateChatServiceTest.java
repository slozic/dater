package com.slozic.dater.services;

import com.slozic.dater.dto.response.chat.ChatMessageDto;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.models.ChatMessage;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import com.slozic.dater.repositories.ChatMessageRepository;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.services.chat.DateChatService;
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

    @Test
    void sendMessage_shouldRouteOwnerMessageToAcceptedAttendee() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            final ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        final ChatMessageDto dto = dateChatService.sendMessage(dateId.toString(), ownerId, "hello");

        assertThat(dto.senderId()).isEqualTo(ownerId.toString());
        assertThat(dto.recipientId()).isEqualTo(acceptedAttendeeId.toString());
        assertThat(dto.message()).isEqualTo("hello");
    }

    @Test
    void sendMessage_shouldRouteAcceptedAttendeeMessageToOwner() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).build();
        final DateAttendee acceptedAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateId, acceptedAttendeeId))
                .status(ACCEPTED)
                .build();
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(date));
        when(dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId))
                .thenReturn(List.of(acceptedAttendee));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            final ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        final ChatMessageDto dto = dateChatService.sendMessage(dateId.toString(), acceptedAttendeeId, "hi owner");

        assertThat(dto.senderId()).isEqualTo(acceptedAttendeeId.toString());
        assertThat(dto.recipientId()).isEqualTo(ownerId.toString());
    }

    @Test
    void getMessagesForDate_shouldRejectNonParticipants() {
        final UUID dateId = UUID.randomUUID();
        final UUID ownerId = UUID.randomUUID();
        final UUID acceptedAttendeeId = UUID.randomUUID();
        final UUID randomUser = UUID.randomUUID();
        final Date date = Date.builder().id(dateId).createdBy(ownerId).build();
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
}
