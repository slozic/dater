package com.slozic.dater.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDateEventRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        Double latitude,
        Double longitude,
        @NotBlank String scheduledTime) {
};
