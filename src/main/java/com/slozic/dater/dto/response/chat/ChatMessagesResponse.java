package com.slozic.dater.dto.response.chat;

import java.util.List;

public record ChatMessagesResponse(String dateId, List<ChatMessageDto> messages) {
}
