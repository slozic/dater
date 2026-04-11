package com.slozic.dater.dto.enums;

public enum UserReportReason {
    SPAM,
    HARASSMENT,
    INAPPROPRIATE,
    IMPERSONATION,
    OTHER;

    public static UserReportReason fromString(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Report reason must not be blank.");
        }
        try {
            return UserReportReason.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported report reason: " + rawValue);
        }
    }
}
