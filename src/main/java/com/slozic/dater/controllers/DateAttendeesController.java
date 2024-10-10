package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.attendees.DateAttendeeResponse;
import com.slozic.dater.dto.response.attendees.DateAttendeeStatusResponse;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateAttendeesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dates/{id}/attendees")
@RequiredArgsConstructor
@Slf4j
public class DateAttendeesController {
    private final DateAttendeesService dateAttendeesService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public DateAttendeeResponse getDateAttendeeRequests(@PathVariable("id") String dateId) {
        return dateAttendeesService.getAllDateAttendeeRequests(dateId);
    }

    @GetMapping("/status")
    public DateAttendeeStatusResponse getMyAttendeeStatus(@PathVariable("id") String dateId) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateAttendeesService.getDateAttendeeStatus(dateId, currentUserId);
    }

    @PostMapping
    public DateAttendeeStatusResponse addAttendeeToDate(@PathVariable("id") String dateId) throws UnauthorizedException {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateAttendeesService.addAttendeeToDate(dateId, currentUserId);
    }

    @PutMapping("/{userId}")
    public DateAttendeeStatusResponse acceptDateAttendee(@PathVariable("id") String dateId, @PathVariable("userId") String userId)
            throws UnauthorizedException {
        return dateAttendeesService.acceptAttendeeRequest(dateId, userId);
    }

    @DeleteMapping("/{userId}")
    public DateAttendeeStatusResponse rejectDateAttendee(@PathVariable("id") String dateId, @PathVariable("userId") String attendeeId)
            throws UnauthorizedException {
        return dateAttendeesService.rejectDateAttendeeRequest(dateId, attendeeId);
    }
}
