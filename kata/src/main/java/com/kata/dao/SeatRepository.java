package com.kata.dao;

import com.kata.enums.SeatStatus;
import com.kata.dao.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s JOIN FETCH s.event WHERE s.event.id = :eventId AND s.status = :status")
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);
}
