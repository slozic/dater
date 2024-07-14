package com.slozic.dater.exceptions;

import lombok.Getter;

public class DateEventException extends BusinessException {
    public DateEventException(String message) {
        super(message);
    }

    public DateEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public DateEventException(Throwable cause) {
        super(cause);
    }
}
