package com.slozic.dater.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportUserRequest(
        @NotBlank(message = "must not be blank")
        String reason,
        @Size(max = 1000, message = "must be at most 1000 characters")
        String note
) {
}
