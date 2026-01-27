package com.banking.platform.exception;

public class ValidationFailureException  extends RuntimeException {
    public ValidationFailureException(String message) {
        super(message);
    }
}
