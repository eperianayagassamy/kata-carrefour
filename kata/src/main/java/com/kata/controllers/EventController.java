package com.kata.controllers;

import com.kata.business.EventService;
import com.kata.dto.SeatResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for exploring events and checking seat availability.
 * Provides read-only access to the event topology and current seat statuses.
 */
@RestController
@RequestMapping("${app.api.base-path}/events")
@RequiredArgsConstructor
@Tag(name = "Event Discovery", description = "Endpoints for browsing events and checking seat availability")
public class EventController {

    private final EventService eventService;

    /**
     * Retrieves the list of available seats for a specific event.
     * <p>
     * A seat is considered available only if:
     * <ul>
     * <li>Its status in the database is 'AVAILABLE'</li>
     * <li>It is not currently locked in the temporary hold cache</li>
     * </ul>
     * This endpoint ensures that users only see seats that can effectively be reserved.
     * </p>
     *
     * @param eventId the unique identifier of the event
     * @return a {@link ResponseEntity} containing a list of {@link SeatResponseDTO}
     * representing the available seats
     */
    @GetMapping("/{eventId}/seats")
    @Operation(
            summary = "Get available seats",
            description = "Returns a list of seats that are both AVAILABLE in the database and not currently HELD in the cache."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved available seats",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found",
                    content = @Content
            )
    })
    public ResponseEntity<List<SeatResponseDTO>> getAvailableSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getAvailableSeats(eventId));
    }
}
