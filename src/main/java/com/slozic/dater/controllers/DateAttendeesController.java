package com.slozic.dater.controllers;

import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.response.DateAttendeeResponse;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateAttendeesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public DateAttendeeResponse getDateAttendees(@PathVariable("id") String dateId) {
        return dateAttendeesService.getAllDateAttendees(dateId);
    }

    @GetMapping("/status")
    public JoinDateStatus getDateAttendeeStatus(@PathVariable("id") String dateId) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateAttendeesService.getDateAttendeeStatus(dateId, currentUserId);
    }

    @PostMapping
    public ResponseEntity<?> addAttendeeToDate(@PathVariable("id") String dateId) throws UnauthorizedException {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        JoinDateStatus joinDateStatus = dateAttendeesService.addAttendeeToDate(dateId, currentUserId);
        return ResponseEntity.ok("User added successfully! Status: " + joinDateStatus);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> acceptDateAttendee(@PathVariable("id") String dateId, @PathVariable("userId") String userId)
            throws UnauthorizedException {
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        JoinDateStatus joinDateStatus = dateAttendeesService.acceptAttendeeRequest(dateId, userId, currentUser);
        return ResponseEntity.ok(joinDateStatus);
    }
}
