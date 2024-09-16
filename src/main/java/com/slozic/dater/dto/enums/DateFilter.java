package com.slozic.dater.dto.enums;

import java.util.Optional;

public enum DateFilter {
    ALL,
    OWNED,
    REQUESTED;

    public static DateFilter fromString(Optional<String> value) {
        try {
            return DateFilter.valueOf(value.isPresent() ? value.get().toUpperCase() : DateFilter.ALL.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for DateFilter: " + value);
        }
    }
}
