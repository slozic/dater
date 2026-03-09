package com.slozic.dater.dto.response.notifications;

import com.slozic.dater.models.AppNotification;

public record NotificationDto(
        String id,
        String type,
        String title,
        String body,
        String relatedDateId,
        String createdAt,
        boolean read
) {
    public static NotificationDto from(final AppNotification notification) {
        return new NotificationDto(
                notification.getId().toString(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getRelatedDateId() == null ? null : notification.getRelatedDateId().toString(),
                notification.getCreatedAt().toString(),
                notification.getReadAt() != null
        );
    }
}
