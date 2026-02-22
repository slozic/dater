package com.slozic.dater.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotBlank String birthday,
        @NotBlank String gender) {
}
