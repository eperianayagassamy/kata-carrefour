package com.kata.business;

import com.kata.dao.SeatRepository;
import com.kata.dto.SeatResponseDTO;
import com.kata.enums.SeatStatus;
import com.kata.dao.entities.Seat;
import com.kata.business.models.SeatHold;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private SeatRepository seatRepository;

    private Map<Long, SeatHold> holdCache;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        holdCache = new HashMap<>();
        eventService = new EventService(seatRepository, holdCache);
    }

    @Test
    @DisplayName("Should return only seats that are available in DB AND not held in cache")
    void getAvailableSeats_Success() {
        Long eventId = 1L;

        Seat seat1 = new Seat();
        seat1.setId(101L);
        seat1.setSeatNumber("A1");


        Seat seat2 = new Seat();
        seat2.setId(102L);
        seat2.setSeatNumber("A2");

        when(seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE))
                .thenReturn(List.of(seat1, seat2));

        holdCache.put(102L, new SeatHold(102L, LocalDateTime.now().plusMinutes(10)));

        List<SeatResponseDTO> result = eventService.getAvailableSeats(eventId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(101L);
        assertThat(result.get(0).seatNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("Should return seat if hold in cache is expired")
    void getAvailableSeats_WhenHoldIsExpired() {
        Long eventId = 1L;
        Seat seat1 = new Seat();
        seat1.setId(101L);
        seat1.setSeatNumber("A1");

        when(seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE))
                .thenReturn(List.of(seat1));

        holdCache.put(101L, new SeatHold(101L, LocalDateTime.now().minusMinutes(5)));

        List<SeatResponseDTO> result = eventService.getAvailableSeats(eventId);

        assertThat(result).hasSize(1);
    }
}