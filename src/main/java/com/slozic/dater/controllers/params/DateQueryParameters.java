package com.slozic.dater.controllers.params;

import java.util.Optional;

public record DateQueryParameters(
        Optional<String> filter,
        Optional<Integer> size,
        Optional<String> sort,
        Optional<Double> latitude,
        Optional<Double> longitude,
        Optional<Double> radiusKm) {
}