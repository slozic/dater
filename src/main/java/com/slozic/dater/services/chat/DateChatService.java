package com.slozic.dater.services.chat;

import com.slozic.dater.dto.response.chat.ChatMessageDto;
import com.slozic.dater.dto.response.chat.ChatMessagesResponse;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.models.ChatMessage;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.ChatMessageRepository;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DateChatService {
    private final DateEventRepository dateEventRepository;
    private final DateAttendeeRepository dateAttendeeRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public ChatMessagesResponse getMessagesForDate(final String dateId, final UUID currentUserId) {
        final UUID parsedDateId = UUID.fromString(dateId);
        final ChatParticipants participants = resolveParticipants(parsedDateId);
        validateParticipantAccess(currentUserId, participants);

        final List<ChatMessageDto> messages = chatMessageRepository.findAllByDateIdOrderByCreatedAtAsc(parsedDateId)
                .stream()
                .map(ChatMessageDto::from)
                .toList();
        return new ChatMessagesResponse(dateId, messages);
    }

    @Transactional
    public ChatMessageDto sendMessage(
            final String dateId,
            final UUID currentUserId,
            final String rawMessage
    ) {
        final UUID parsedDateId = UUID.fromString(dateId);
        final ChatParticipants participants = resolveParticipants(parsedDateId);
        validateParticipantAccess(currentUserId, participants);
        final String message = rawMessage == null ? "" : rawMessage.trim();
        if (message.isEmpty()) {
            throw new DateEventException("Message must not be blank.");
        }
        final UUID recipientId = participants.ownerId().equals(currentUserId)
                ? participants.acceptedAttendeeId()
                : participants.ownerId();
        final ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .dateId(parsedDateId)
                .senderId(currentUserId)
                .recipientId(recipientId)
                .message(message)
                .build());
        return ChatMessageDto.from(saved);
    }

    private ChatParticipants resolveParticipants(final UUID dateId) {
        final Date date = dateEventRepository.findById(dateId)
                .orElseThrow(() -> new DateEventException("Date event not found: " + dateId));
        final UUID ownerId = date.getCreatedBy();
        final List<DateAttendee> acceptedAttendees =
                dateAttendeeRepository.findAcceptedAttendeesForDateExcludingUser(dateId, ownerId);
        if (acceptedAttendees.isEmpty()) {
            throw new DateEventException("Chat is available only after attendee acceptance.");
        }
        final UUID acceptedAttendeeId = acceptedAttendees.get(0).getId().getAttendeeId();
        return new ChatParticipants(ownerId, acceptedAttendeeId);
    }

    private void validateParticipantAccess(final UUID currentUserId, final ChatParticipants participants) {
        if (!participants.ownerId().equals(currentUserId) && !participants.acceptedAttendeeId().equals(currentUserId)) {
            throw new DateEventAccessPermissionException("You do not have access to this date chat.");
        }
    }

    private record ChatParticipants(UUID ownerId, UUID acceptedAttendeeId) {
    }
}
