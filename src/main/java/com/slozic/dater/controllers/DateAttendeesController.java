package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dates/{id}/attendees")
@RequiredArgsConstructor
@Slf4j
public class DateAttendeesController {

    private final DateAttendeeRepository dateAttendeeRepository;

    @GetMapping()
    public List<DateAttendeeDto> getDateAttendees(@PathVariable("id") String dateId) {
        final List<DateAttendee> dateAttendeesList = dateAttendeeRepository.findAllByDateId(UUID.fromString(dateId));
        return dateAttendeesList.stream()
                                .filter(attendee -> !attendee.getAttendeeId().equals(attendee.getDate().getCreatedBy()))
                                .map(dateAttendee -> DateAttendeeDto.from(dateAttendee))
                                .collect(Collectors.toList());
    }

    @PostMapping()
    @Transactional
    public ResponseEntity<?> addDateAttendee(@PathVariable("id") String dateId) throws UnauthorizedException {
        final UUID currentUserId = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUserId, UUID.fromString(dateId))
                                .ifPresentOrElse( attendee -> {
                                                 throw new IllegalArgumentException("User already requested to join!");
                                             },
                                    () -> {
                                        dateAttendeeRepository.save(DateAttendee.builder()
                                                                                      .dateId(UUID.fromString(dateId))
                                                                                      .attendeeId(currentUserId)
                                                                                      .build());
                                        });
        return ResponseEntity.ok("User added successfully");
    }

    @PutMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> acceptDateAttendee(@PathVariable("id") String dateId, @PathVariable("userId") String userId)
            throws UnauthorizedException {
        final UUID currentUser = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(UUID.fromString(userId), UUID.fromString(dateId))
                              .ifPresentOrElse( attendee -> {
                                      if(!attendee.getDate().getCreatedBy().equals(currentUser)) {
                                          throw new IllegalArgumentException("Date request can only be accepted by user who created it!");
                                      }
                                      attendee.setAccepted(true);
                                      dateAttendeeRepository.save(attendee);
                                },
                                () -> {
                                    throw new IllegalArgumentException("User could not be found!");
                                });
        return ResponseEntity.ok("User accepted!");
    }
}
