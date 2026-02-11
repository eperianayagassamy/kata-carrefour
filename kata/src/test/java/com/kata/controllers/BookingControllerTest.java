package com.kata.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kata.business.SeatBookingService;
import com.kata.dto.SeatReservationRequestDTO;
import com.kata.exceptions.ReservationExpiredException;
import com.kata.exceptions.SeatAlreadyHoldByAnotherUserException;
import com.kata.exceptions.SeatAlreadySoldException;
import com.kata.exceptions.SeatNotAvailableException;
import com.kata.exceptions.SeatNotFoundException;
import com.kata.exceptions.SeatNotHoldException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SeatBookingService seatBookingService;

    private final String BASE_URL = "/api/v1/bookings";

    @Test
    @DisplayName("POST /hold should return 201 Created when successful")
    void holdSeat_Success() throws Exception {
        SeatReservationRequestDTO request = new SeatReservationRequestDTO(1L, 100L);

        mockMvc.perform(post(BASE_URL + "/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /hold should return 409 when seat is already sold")
    void holdSeat_Conflict_seatAlreadySold() throws Exception {
        SeatReservationRequestDTO request = new SeatReservationRequestDTO(1L, 100L);

        doThrow(new SeatAlreadySoldException("Already sold"))
                .when(seatBookingService).holdSeat(any());

        mockMvc.perform(post(BASE_URL + "/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /hold should return 409 when seat is already locked")
    void holdSeat_Conflict_seatAlreadyLocked() throws Exception {
        SeatReservationRequestDTO request = new SeatReservationRequestDTO(1L, 100L);

        doThrow(new SeatNotAvailableException("Seat already locked"))
                .when(seatBookingService).holdSeat(any());

        mockMvc.perform(post(BASE_URL + "/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /hold should return 404 when seat is not found")
    void holdSeat_NotFound() throws Exception {
        SeatReservationRequestDTO request = new SeatReservationRequestDTO(1L, 100L);

        doThrow(new SeatNotFoundException("Seat not found"))
                .when(seatBookingService).holdSeat(any());

        mockMvc.perform(post(BASE_URL + "/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /confirm/{id} should return 200 OK when successful")
    void confirmBooking_Success() throws Exception {
        mockMvc.perform(post(BASE_URL + "/confirm/1")
                        .param("userId", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /confirm/{id} should return 401 when user ID is incorrect")
    void confirmBooking_WrongUser() throws Exception {
        doThrow(new SeatAlreadyHoldByAnotherUserException("This seat is already booked by another user."))
                .when(seatBookingService).confirmPayment(1L, 999L);

        mockMvc.perform(post(BASE_URL + "/confirm/1")
                        .param("userId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /confirm/{id} should return 410 when reservation is expired")
    void confirmBooking_SeatReservationExpired() throws Exception {
        doThrow(new ReservationExpiredException("The session is expired."))
                .when(seatBookingService).confirmPayment(1L, 999L);

        mockMvc.perform(post(BASE_URL + "/confirm/1")
                        .param("userId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());
    }

    @Test
    @DisplayName("POST /confirm/{id} should return 409 when seat is not hold")
    void confirmBooking_SeatNotHold() throws Exception {
        doThrow(new SeatNotHoldException("Seat not hold"))
                .when(seatBookingService).confirmPayment(1L, 999L);

        mockMvc.perform(post(BASE_URL + "/confirm/1")
                        .param("userId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}