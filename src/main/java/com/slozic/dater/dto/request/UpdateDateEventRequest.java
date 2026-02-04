package com.slozic.dater.dto.request;

public record UpdateDateEventRequest(
        String title,
        String description,
        String location,
        Double latitude,
        Double longitude,
        String scheduledTime) {
}
