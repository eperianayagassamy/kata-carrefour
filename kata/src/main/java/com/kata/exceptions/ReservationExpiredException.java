package com.kata.exceptions;

public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(final String message) { super(message);}
}
