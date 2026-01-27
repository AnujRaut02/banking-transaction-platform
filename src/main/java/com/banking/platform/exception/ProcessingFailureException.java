package com.banking.platform.exception;

public class ProcessingFailureException extends RuntimeException {
    public ProcessingFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
