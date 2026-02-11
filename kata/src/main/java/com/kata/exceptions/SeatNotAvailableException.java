package com.kata.exceptions;

public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(final String message) { super(message);}
}
