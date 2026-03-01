package com.slozic.dater.dto.response.chat;

import com.slozic.dater.models.ChatMessage;

public record ChatMessageDto(
        String id,
        String dateId,
        String senderId,
        String recipientId,
        String message,
        String createdAt) {
    public static ChatMessageDto from(final ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getId().toString(),
                chatMessage.getDateId().toString(),
                chatMessage.getSenderId().toString(),
                chatMessage.getRecipientId().toString(),
                chatMessage.getMessage(),
                chatMessage.getCreatedAt().toString());
    }
}
