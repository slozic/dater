package com.slozic.dater.exceptions.user;

import com.slozic.dater.exceptions.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
