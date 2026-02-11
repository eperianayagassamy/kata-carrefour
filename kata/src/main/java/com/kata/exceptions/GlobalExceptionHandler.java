package com.kata.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSeatNotAvailable(SeatNotAvailableException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SeatAlreadySoldException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadySold(SeatAlreadySoldException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReservationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleReservationExpired(ReservationExpiredException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.GONE, ex.getMessage()), HttpStatus.GONE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSeatNotFound(SeatNotFoundException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SeatNotHoldException.class)
    public ResponseEntity<ErrorResponse> handleSeatNotHold(SeatNotHoldException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SeatAlreadyHoldByAnotherUserException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadyHoldByAnotherUserException(SeatAlreadyHoldByAnotherUserException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message, LocalDateTime.now());
    }
}
