package com.kata.exceptions;

public class SeatAlreadyHoldByAnotherUserException extends RuntimeException {
    public SeatAlreadyHoldByAnotherUserException(String message) {
        super(message);
    }
}
