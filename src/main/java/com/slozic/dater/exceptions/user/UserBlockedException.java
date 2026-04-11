package com.slozic.dater.exceptions.user;

import com.slozic.dater.exceptions.BusinessException;

public class UserBlockedException extends BusinessException {
    public UserBlockedException(final String message) {
        super(message);
    }
}
