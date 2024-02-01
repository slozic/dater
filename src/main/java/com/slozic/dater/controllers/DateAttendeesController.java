package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateAttendeesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dates/{id}/attendees")
@RequiredArgsConstructor
@Slf4j
public class DateAttendeesController {

    private final DateAttendeesService dateAttendeesService;

    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping()
    public List<DateAttendeeDto> getDateAttendees(@PathVariable("id") String dateId) {
        return dateAttendeesService.getDateAttendeeDtos(dateId);
    }

    @PostMapping()
    @Transactional
    public ResponseEntity<?> addDateAttendee(@PathVariable("id") String dateId) throws UnauthorizedException {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        dateAttendeesService.addAttendeeRequest(dateId, currentUserId);
        return ResponseEntity.ok("User added successfully");
    }

    @PutMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> acceptDateAttendee(@PathVariable("id") String dateId, @PathVariable("userId") String userId)
            throws UnauthorizedException {
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        dateAttendeesService.acceptAttendeeRequest(dateId, userId, currentUser);
        return ResponseEntity.ok("User accepted!");
    }
}
