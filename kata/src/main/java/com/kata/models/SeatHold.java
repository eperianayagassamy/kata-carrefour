package com.kata.models;

import java.time.LocalDateTime;

public record SeatHold(Long userId, LocalDateTime expiresAt) {}
