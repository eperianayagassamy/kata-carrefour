package com.kata.dto;

import jakarta.validation.constraints.NotNull;

public record SeatReservationRequestDTO(@NotNull Long seatId, @NotNull Long userId) {}
