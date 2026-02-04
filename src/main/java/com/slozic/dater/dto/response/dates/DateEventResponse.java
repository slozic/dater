package com.slozic.dater.dto.response.dates;

public record DateEventResponse(
        String id,
        String title,
        String location,
        Double latitude,
        Double longitude,
        String description,
        String dateOwner,
        String dateOwnerId,
        String dateJoiner,
        String scheduledTime,
        String joinStatus) {
}
