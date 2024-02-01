package com.slozic.dater.exceptions;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public class DateEventException extends Exception {
    String message;
}
