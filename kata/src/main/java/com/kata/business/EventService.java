package com.kata.business;


import com.kata.dao.SeatRepository;
import com.kata.dto.SeatResponseDTO;
import com.kata.enums.SeatStatus;
import com.kata.models.SeatHold;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final SeatRepository seatRepository;
    private final Map<Long, SeatHold> holdCache;


    /**
     * Récupère les sièges disponibles pour un événement.
     * Une place est disponible si :
     * 1. Son statut en base est 'AVAILABLE'
     * 2. Elle n'est pas présente dans le cache de verrouillage (ou expirée)
     * @param eventId l'id de l'évenement
     */
    public List<SeatResponseDTO> getAvailableSeats(Long eventId) {
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE)
                .stream()
                .filter(seat -> !isSeatHeld(seat.getId()))
                .map(seat -> new SeatResponseDTO(
                        seat.getId(),
                        seat.getSeatNumber(),
                        "AVAILABLE"
                ))
                .collect(Collectors.toList());
    }

    private boolean isSeatHeld(Long seatId) {
        SeatHold hold = holdCache.get(seatId);
        if (hold == null) return false;

        return hold.expiresAt().isAfter(LocalDateTime.now());
    }
}