package com.slozic.dater.controllers.params;

import java.util.Optional;

public record DateQueryParameters(
        Optional<String> filter,
        Optional<Integer> size,
        Optional<String> sort) {
}