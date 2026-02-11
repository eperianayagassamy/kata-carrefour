package com.kata.dao;

import com.kata.enums.SeatStatus;
import com.kata.models.Event;
import com.kata.models.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Should persist Event and its Seats correctly via Cascade")
    void saveEvent_WithSeats_Success() {
        Event event = new Event();
        event.setTitle("Rock Festival");
        event.setDateTime(LocalDateTime.now().plusDays(10));

        Seat seat1 = new Seat();
        seat1.setSeatNumber("B1");
        seat1.setStatus(SeatStatus.AVAILABLE);
        seat1.setEvent(event);

        event.setSeats(List.of(seat1));

        Event savedEvent = eventRepository.save(event);
        entityManager.flush();
        entityManager.clear();

        Event retrievedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(retrievedEvent.getTitle()).isEqualTo("Rock Festival");
        assertThat(retrievedEvent.getSeats()).hasSize(1);
        assertThat(retrievedEvent.getSeats().get(0).getSeatNumber()).isEqualTo("B1");
    }

    @Test
    @DisplayName("Should remove Seats when Event is deleted")
    void deleteEvent_ShouldRemoveSeats() {
        Event event = new Event();
        event.setTitle("To be deleted");
        event.setDateTime(LocalDateTime.now());

        Seat seat = new Seat();
        seat.setSeatNumber("C1");
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setEvent(event);
        event.setSeats(List.of(seat));

        Event savedEvent = eventRepository.save(event);
        entityManager.flush();

        eventRepository.delete(savedEvent);
        entityManager.flush();

        assertThat(eventRepository.findById(savedEvent.getId())).isEmpty();
        List<Seat> orphanSeats = entityManager.getEntityManager()
                .createQuery("SELECT s FROM Seat s WHERE s.seatNumber = 'C1'", Seat.class)
                .getResultList();
        assertThat(orphanSeats).isEmpty();
    }

    @Test
    @DisplayName("Should fail when title is null")
    void saveEvent_NullTitle_ShouldFail() {
        Event event = new Event();
        event.setTitle(null);
        event.setDateTime(LocalDateTime.now());

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when dateTime is null")
    void saveEvent_NullDate_ShouldFail() {
        Event event = new Event();
        event.setTitle("Concert");
        event.setDateTime(null);

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}