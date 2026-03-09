package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.notifications.NotificationListResponse;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public NotificationListResponse getMyNotifications() {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return notificationService.getUserNotifications(currentUserId);
    }

    @PutMapping("/read-all")
    public void markAllAsRead() {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        notificationService.markAllAsRead(currentUserId);
    }
}
