package com.slozic.dater.exceptions.attendee;

import com.slozic.dater.exceptions.BusinessException;

public class AttendeeNotFoundException extends BusinessException {
    public AttendeeNotFoundException(String message) {
        super(message);
    }
}
