package com.kata.dao;

import com.kata.enums.SeatStatus;
import com.kata.dao.entities.Event;
import com.kata.dao.entities.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SeatRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("Should save seat with string representation of Enum")
    void saveSeat_EnumStringMapping_Success() {
        Event event = createAndPersistEvent();
        Seat seat = new Seat();
        seat.setSeatNumber("A1");
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setEvent(event);

        Seat savedSeat = seatRepository.save(seat);
        entityManager.flush();
        entityManager.clear();

        Seat retrievedSeat = seatRepository.findById(savedSeat.getId()).orElseThrow();
        assertThat(retrievedSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);

        String statusInDb = (String) entityManager.getEntityManager()
                .createNativeQuery("SELECT status FROM seat WHERE id = " + savedSeat.getId())
                .getSingleResult();
        assertThat(statusInDb).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("Should fail when seatNumber is null")
    void saveSeat_NullNumber_ShouldFail() {
        Seat seat = new Seat();
        seat.setSeatNumber(null);
        seat.setStatus(SeatStatus.AVAILABLE);

        assertThatThrownBy(() -> {
            seatRepository.save(seat);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail when status is null")
    void saveSeat_NullStatus_ShouldFail() {
        Seat seat = new Seat();
        seat.setSeatNumber("A1");
        seat.setStatus(null);

        assertThatThrownBy(() -> {
            seatRepository.save(seat);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Helper pour créer un événement parent car un Seat
     * est souvent dépendant de l'existence d'un Event.
     */
    private Event createAndPersistEvent() {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setDateTime(LocalDateTime.now());
        return entityManager.persist(event);
    }
}