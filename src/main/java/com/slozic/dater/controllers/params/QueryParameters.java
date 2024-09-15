package com.slozic.dater.controllers.params;

import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

public record QueryParameters(
        @PathVariable("filter") Optional<Integer> pageSize) {
}