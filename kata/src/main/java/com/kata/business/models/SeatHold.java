package com.kata.business.models;

import java.time.LocalDateTime;

public record SeatHold(Long userId, LocalDateTime expiresAt) {}
