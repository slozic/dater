package com.slozic.dater.exceptions.handlers;

import com.slozic.dater.exceptions.*;
import com.slozic.dater.exceptions.attendee.AttendeeNotFoundException;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.exceptions.dateimage.DateImageException;
import com.slozic.dater.exceptions.user.UserNotFoundException;
import com.slozic.dater.exceptions.user.UserProfileImageException;
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

    @ExceptionHandler(FileStorageException.class)
    ResponseEntity<ErrorResponse> handleErrorOnImageStorage(final FileStorageException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .title("Error occurred while accessing the image file.")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(AttendeeNotFoundException.class)
    ResponseEntity<ErrorResponse> handleAttendeeNotFound(final AttendeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .title("Date attendee could not be found!")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UserProfileImageException.class)
    ResponseEntity<ErrorResponse> handleProfileImageNotFound(final UserProfileImageException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .title("User profile image could not be found")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ErrorResponse> handleUserNotFound(final UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .title("User could not be found!")
                        .detail(ex.getMessage())
                        .build());
    }
}
