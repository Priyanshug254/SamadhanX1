package com.samadhanx.common.exception;

public class SamadhanXException extends RuntimeException {
    public SamadhanXException(String message) {
        super(message);
    }

    public SamadhanXException(String message, Throwable cause) {
        super(message, cause);
    }
}
