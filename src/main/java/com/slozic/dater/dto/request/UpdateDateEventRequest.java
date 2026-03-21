package com.slozic.dater.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateDateEventRequest(
        @Pattern(regexp = ".*\\S.*", message = "title must not be blank")
        String title,
        @Pattern(regexp = ".*\\S.*", message = "description must not be blank")
        String description,
        @Pattern(regexp = ".*\\S.*", message = "location must not be blank")
        String location,
        Double latitude,
        Double longitude,
        @Pattern(regexp = ".*\\S.*", message = "scheduledTime must not be blank")
        String scheduledTime) {
}
