package com.slozic.dater.exceptions.handlers;

import com.slozic.dater.exceptions.FileStorageException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.exceptions.attendee.AttendeeAlreadyExistsException;
import com.slozic.dater.exceptions.attendee.AttendeeNotFoundException;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.exceptions.dateevent.DateEventNotFoundException;
import com.slozic.dater.exceptions.dateimage.DateImageAccessException;
import com.slozic.dater.exceptions.dateimage.DateImageException;
import com.slozic.dater.exceptions.user.UserProfileImageAccessException;
import com.slozic.dater.exceptions.user.UserNotFoundException;
import com.slozic.dater.exceptions.user.UserProfileImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.DateTimeException;

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
    ResponseEntity<ErrorResponse> handleDateEventException(final DateEventException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .title("Date event request failed")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(DateEventNotFoundException.class)
    ResponseEntity<ErrorResponse> handleDateEventNotFound(final DateEventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .title("Date event not found")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(final IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .title("Invalid request")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(DateTimeException.class)
    ResponseEntity<ErrorResponse> handleDateTimeException(final DateTimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .title("Invalid date/time format")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(AttendeeAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleAttendeeAlreadyExists(final AttendeeAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .title("Attendee request already exists")
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
                        .title("Image storage error")
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

    @ExceptionHandler(UserProfileImageAccessException.class)
    ResponseEntity<ErrorResponse> handleProfileImageAccessException(final UserProfileImageAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .title("User profile image access forbidden")
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

    @ExceptionHandler(DateEventAccessPermissionException.class)
    ResponseEntity<ErrorResponse> handleDateEventPermissionException(final DateEventAccessPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .title("Date event access forbidden")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(DateImageAccessException.class)
    ResponseEntity<ErrorResponse> handleDateImageAccessException(final DateImageAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .title("Date event image access forbidden")
                        .detail(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidationException(final MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .title("Validation failed")
                        .detail(detail)
                        .build());
    }

}
