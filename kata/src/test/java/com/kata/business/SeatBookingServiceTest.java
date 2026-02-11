package com.kata.business;

import com.kata.dao.SeatRepository;
import com.kata.dto.SeatReservationRequestDTO;
import com.kata.enums.SeatStatus;
import com.kata.exceptions.ReservationExpiredException;
import com.kata.exceptions.SeatAlreadyHoldByAnotherUserException;
import com.kata.exceptions.SeatAlreadySoldException;
import com.kata.exceptions.SeatNotAvailableException;
import com.kata.models.Seat;
import com.kata.models.SeatHold;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatBookingServiceTest {

    @Mock
    private SeatRepository seatRepository;

    private Map<Long, SeatHold> holdCache;
    private SeatBookingService bookingService;

    @BeforeEach
    void setUp() {
        holdCache = new HashMap<>();
        bookingService = new SeatBookingService(seatRepository, holdCache);
    }

    @Nested
    @DisplayName("Hold Seat Tests")
    class HoldSeatTests {

        @Test
        @DisplayName("Should successfully hold an available seat")
        void holdSeat_Success() {
            Long seatId = 1L;
            Long userId = 100L;
            SeatReservationRequestDTO request = new SeatReservationRequestDTO(seatId, userId);

            Seat seat = new Seat();
            seat.setId(seatId);
            seat.setStatus(SeatStatus.AVAILABLE);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            bookingService.holdSeat(request);

            assertThat(holdCache).containsKey(seatId);
            assertThat(holdCache.get(seatId).userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw exception if seat is already in cache and not expired")
        void holdSeat_ConflictInCache() {
            Long seatId = 1L;
            holdCache.put(seatId, new SeatHold(200L, LocalDateTime.now().plusMinutes(5)));
            SeatReservationRequestDTO request = new SeatReservationRequestDTO(seatId, 100L);

            assertThatThrownBy(() -> bookingService.holdSeat(request))
                    .isInstanceOf(SeatNotAvailableException.class)
                    .hasMessage("Seat already locked");
        }

        @Test
        @DisplayName("Should throw exception if seat is already SOLD in DB")
        void holdSeat_AlreadySold() {
            Long seatId = 1L;
            Seat seat = new Seat();
            seat.setStatus(SeatStatus.SOLD);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
            SeatReservationRequestDTO request = new SeatReservationRequestDTO(seatId, 100L);

            assertThatThrownBy(() -> bookingService.holdSeat(request))
                    .isInstanceOf(SeatAlreadySoldException.class);
        }
    }

    @Nested
    @DisplayName("Confirm Payment Tests")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Should successfully confirm payment and update DB")
        void confirmPayment_Success() {
            Long seatId = 1L;
            Long userId = 100L;
            holdCache.put(seatId, new SeatHold(userId, LocalDateTime.now().plusMinutes(5)));

            Seat seat = new Seat();
            seat.setId(seatId);
            seat.setStatus(SeatStatus.AVAILABLE);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            bookingService.confirmPayment(seatId, userId);

            assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
            assertThat(holdCache).doesNotContainKey(seatId);
            verify(seatRepository, times(1)).save(seat);
        }

        @Test
        @DisplayName("Should throw exception if reservation is expired in cache")
        void confirmPayment_Expired() {
            Long seatId = 1L;
            holdCache.put(seatId, new SeatHold(100L, LocalDateTime.now().minusMinutes(1)));

            assertThatThrownBy(() -> bookingService.confirmPayment(seatId, 100L))
                    .isInstanceOf(ReservationExpiredException.class);

            assertThat(holdCache).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception if userId does not match the one in cache")
        void confirmPayment_WrongUser() {
            Long seatId = 1L;
            holdCache.put(seatId, new SeatHold(100L, LocalDateTime.now().plusMinutes(5)));

            assertThatThrownBy(() -> bookingService.confirmPayment(seatId, 999L))
                    .isInstanceOf(SeatAlreadyHoldByAnotherUserException.class)
                    .hasMessage("This seat is already booked by another user.");
        }
    }
}