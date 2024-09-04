package com.slozic.dater.exceptions.handlers;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ErrorResponse {
    @NonNull
    @Builder.Default
    private Integer status = 500;

    @NonNull
    private String title;

    @NonNull
    private String detail;
}
