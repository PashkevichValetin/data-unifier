package com.pashcevich.data_unifier.exception;

public class AdapterException extends RuntimeException {
    public AdapterException(String message) {
        super(message);
    }
    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
