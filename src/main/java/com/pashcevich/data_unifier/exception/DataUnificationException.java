package com.pashcevich.data_unifier.exception;

public class DataUnificationException extends RuntimeException {

    public DataUnificationException(String message) {
        super(message);
    }

    public DataUnificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
