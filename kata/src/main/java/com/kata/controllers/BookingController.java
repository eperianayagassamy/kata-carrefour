package com.kata.controllers;

import com.kata.business.SeatBookingService;
import com.kata.dto.SeatReservationRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing seat bookings and reservations.
 * Provides endpoints to temporarily hold seats and confirm final purchases.
 */
@RestController
@RequestMapping("${app.api.base-path}/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Endpoints for holding and confirming seat reservations")
public class BookingController {

    private final SeatBookingService seatBookingService;

    /**
     * Temporarily holds a seat for a specific user.
     * <p>
     * This operation implements a "soft lock" in memory for a limited duration.
     * It will fail if the seat is already sold in the database or currently held in the cache.
     * </p>
     *
     * @param request DTO containing the seat ID and the user ID initiating the hold
     * @return a 201 CREATED status if the seat is holded
     */
    @PostMapping
    @Operation(summary = "Hold a seat", description = "Creates a temporary 10-minute lock on a seat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seat successfully held"),
            @ApiResponse(responseCode = "409", description = "Seat already sold or held"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    public ResponseEntity<Void> holdSeat(@RequestBody SeatReservationRequestDTO request) {
        seatBookingService.holdSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Confirms the payment and finalizes the booking for a held seat.
     * <p>
     * This operation transitions the seat status to 'SOLD' in the database and
     * removes the temporary lock from the cache. It validates that the confirming
     * user is the same one who placed the hold.
     * </p>
     *
     * @param seatId the unique identifier of the seat to be confirmed
     * @param userId the unique identifier of the user performing the checkout
     * @return a 200 OK status if the confirmation is successful
     */
    @PatchMapping("{seatId}")
    @Operation(summary = "Confirm booking", description = "Finalizes the purchase and marks the seat as SOLD.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase confirmed"),
            @ApiResponse(responseCode = "409", description = "Seat not hold"),
            @ApiResponse(responseCode = "410", description = "Hold expired"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User ID does not match the hold")
    })
    public ResponseEntity<Void> confirmBooking(@PathVariable Long seatId, @RequestParam Long userId) {
        seatBookingService.confirmPayment(seatId, userId);
        return ResponseEntity.ok().build();
    }
}
