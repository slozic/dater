package com.slozic.dater.controllers;

import com.slozic.dater.dto.request.SendChatMessageRequest;
import com.slozic.dater.dto.response.chat.ChatMessageDto;
import com.slozic.dater.dto.response.chat.ChatMessagesResponse;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.chat.DateChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dates/{id}/chat/messages")
@RequiredArgsConstructor
public class DateChatController {
    private final DateChatService dateChatService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public ChatMessagesResponse getDateChatMessages(@PathVariable("id") final String dateId) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateChatService.getMessagesForDate(dateId, currentUserId);
    }

    @PostMapping
    public ChatMessageDto sendDateChatMessage(
            @PathVariable("id") final String dateId,
            @Valid @RequestBody final SendChatMessageRequest request
    ) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateChatService.sendMessage(dateId, currentUserId, request.message());
    }
}
