package com.kata.business;

import com.kata.dao.SeatRepository;
import com.kata.dto.SeatReservationRequestDTO;
import com.kata.enums.SeatStatus;
import com.kata.exceptions.ReservationExpiredException;
import com.kata.exceptions.SeatAlreadyHoldByAnotherUserException;
import com.kata.exceptions.SeatAlreadySoldException;
import com.kata.exceptions.SeatNotAvailableException;
import com.kata.exceptions.SeatNotFoundException;
import com.kata.exceptions.SeatNotHoldException;
import com.kata.models.Seat;
import com.kata.models.SeatHold;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatBookingService {

    private final SeatRepository seatRepository;
    private final Map<Long, SeatHold> holdCache;

    @Transactional
    public void holdSeat(SeatReservationRequestDTO seatReservation) {
        SeatHold currentHold = holdCache.get(seatReservation.seatId());
        if (currentHold != null && currentHold.expiresAt().isAfter(LocalDateTime.now())) {
            throw new SeatNotAvailableException("Seat already locked");
        }

        Seat seat = seatRepository.findById(seatReservation.seatId())
                .orElseThrow(() -> new SeatNotFoundException("Seat inexistent"));

        if (seat.getStatus() == SeatStatus.SOLD) {
            throw new SeatAlreadySoldException("Seat is already sold.");
        }

        holdCache.put(seatReservation.seatId(), new SeatHold(seatReservation.userId(), LocalDateTime.now().plusMinutes(10)));
    }

    @Transactional
    public void confirmPayment(Long seatId, Long userId) {
        SeatHold hold = holdCache.get(seatId);

        if(hold == null) {
            throw new SeatNotHoldException("Seat not hold");
        }

        if (hold.expiresAt().isBefore(LocalDateTime.now())) {
            holdCache.remove(seatId);
            throw new ReservationExpiredException("The session is expired.");
        }

        if (!hold.userId().equals(userId)) {
            throw new SeatAlreadyHoldByAnotherUserException("This seat is already booked by another user.");
        }

        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException("Seat inexistent."));
        seat.setStatus(SeatStatus.SOLD);

        holdCache.remove(seatId);

        seatRepository.save(seat);
    }
}