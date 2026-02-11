package com.kata.exceptions;

public class SeatNotHoldException extends RuntimeException {
    public SeatNotHoldException(String message) {
        super(message);
    }
}
