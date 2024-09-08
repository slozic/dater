package com.slozic.dater.exceptions.handlers;

import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.DateImageException;
import com.slozic.dater.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> uncaughtException(final Throwable throwable) {
        log.error("An error occurred leading to a 500", throwable);
        String message = "General exception occurred, please look into the cause";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .detail(throwable.getMessage())
                        .title(message)
                        .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(final UnauthorizedException ex) {
        log.debug("Access denied", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .title("Unauthorised access to API!")
                        .detail("Please login to authenticate and/or contact our customer support if problem persists!")
                        .build());
    }

    @ExceptionHandler(DateEventException.class)
    ResponseEntity<ErrorResponse> handleErrorOnDateEventCreation(final DateEventException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .title("Date event creation failed!")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(DateImageException.class)
    ResponseEntity<ErrorResponse> handleErrorOnDateEventImageCreation(final DateImageException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .title("Date event image creation failed!")
                        .detail(ex.getMessage())
                        .build());
    }
}
