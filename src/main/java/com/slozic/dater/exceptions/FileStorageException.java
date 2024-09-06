package com.slozic.dater.exceptions;

import java.io.IOException;

public class FileStorageException extends BusinessException {
    public FileStorageException(String message, IOException ex) {
        super(message,ex);
    }
}
