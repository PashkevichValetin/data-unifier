package com.pashcevich.data_unifier.exception;

public class UserAdapterException extends RuntimeException {
    public UserAdapterException(String message) {
        super(message);
    }

    public UserAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
