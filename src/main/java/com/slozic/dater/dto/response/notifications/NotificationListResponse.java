package com.slozic.dater.dto.response.notifications;

import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationDto> notifications
) {
}
