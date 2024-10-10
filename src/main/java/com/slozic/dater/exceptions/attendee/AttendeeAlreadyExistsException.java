package com.slozic.dater.exceptions.attendee;

import com.slozic.dater.exceptions.BusinessException;

public class AttendeeAlreadyExistsException extends BusinessException {
    public AttendeeAlreadyExistsException(String message) {
        super(message);
    }
}
