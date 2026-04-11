package com.slozic.dater.dto.response;

public record UserModerationActionResponse(
        String userId,
        boolean reported,
        boolean blocked
) {
}
