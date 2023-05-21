package com.slozic.dater.exceptions;

public class UnauthorizedException extends Exception {
    public UnauthorizedException() {
        this("Unauthorized access");
    }
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
