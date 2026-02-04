package com.slozic.dater.services.attendees;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.response.attendees.DateAttendeeResponse;
import com.slozic.dater.dto.response.attendees.DateAttendeeStatusResponse;
import com.slozic.dater.exceptions.attendee.AttendeeAlreadyExistsException;
import com.slozic.dater.exceptions.attendee.AttendeeNotFoundException;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateAttendeesService {
    private final DateAttendeeRepository dateAttendeeRepository;
    private final DateEventRepository dateEventRepository;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Transactional
    public DateAttendeeResponse getAllDateAttendeeRequests(String dateId) {
        Optional<Date> optionalDate = dateEventRepository.findById(UUID.fromString(dateId));
        if (optionalDate.isEmpty()) {
            throw new DateEventException("Date event not found: " + dateId);
        }

        final List<DateAttendee> dateAttendeesList = dateAttendeeRepository.findAllByIdDateId(UUID.fromString(dateId))
                .stream().filter(dateAttendee -> !dateAttendee.getId().getAttendeeId().equals(optionalDate.get().getCreatedBy())).collect(Collectors.toList());
        return getDateAttendeeResponse(dateId, dateAttendeesList);
    }

    private DateAttendeeResponse getDateAttendeeResponse(String dateid, List<DateAttendee> dateAttendeesList) {
        List<DateAttendeeDto> dateAttendeeDtos = dateAttendeesList.stream()
                .map(DateAttendeeDto::from)
                .collect(Collectors.toList());
        return new DateAttendeeResponse(dateid, dateAttendeeDtos);
    }

    public DateAttendee createDefaultDateAttendee(Date dateCreated) {
        DateAttendee dateAttendee = DateAttendee.builder()
                .id(new DateAttendeeId(dateCreated.getId(), dateCreated.getCreatedBy()))
                .status(JoinDateStatus.ACCEPTED)
                .build();
        return dateAttendeeRepository.save(dateAttendee);
    }

    @Transactional
    public DateAttendeeStatusResponse addAttendeeToDate(String dateId, UUID currentUserId) {
        dateEventRepository.findById(UUID.fromString(dateId)).orElseThrow(() ->
                new DateEventException("Date event not found: " + dateId));
        createNewDateAttendee(dateId, currentUserId);
        return new DateAttendeeStatusResponse(JoinDateStatus.ON_WAITLIST, currentUserId.toString(), dateId);
    }

    private void createNewDateAttendee(String dateId, UUID currentUserId) {
        dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), currentUserId))
                .ifPresentOrElse(
                        attendee -> {
                            throw new AttendeeAlreadyExistsException("Attendee already requested to join date: " + dateId);
                        },
                        () -> dateAttendeeRepository.save(DateAttendee.builder()
                                .id(new DateAttendeeId(UUID.fromString(dateId), currentUserId))
                                .status(JoinDateStatus.ON_WAITLIST)
                                .build()));
    }

    @Transactional
    public DateAttendeeStatusResponse acceptAttendeeRequest(String dateId, String userId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        acceptDateAttendee(dateId, userId, currentUser);
        return new DateAttendeeStatusResponse(JoinDateStatus.ACCEPTED, userId, dateId);
    }

    private void acceptDateAttendee(String dateId, String userId, UUID currentUser) {
        dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), UUID.fromString(userId)))
                .ifPresentOrElse(
                        attendee -> {
                            if (!attendee.getId().getAttendeeId().equals(currentUser)) {
                                attendee.setStatus(JoinDateStatus.ACCEPTED);
                                dateAttendeeRepository.save(attendee);
                            }
                        },
                        () -> {
                            throw new AttendeeNotFoundException("Attendee not found for date: " + dateId);
                        });
    }

    public DateAttendeeStatusResponse getDateAttendeeStatus(String dateId, UUID currentUserId) {
        JoinDateStatus joinDateStatus = dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), currentUserId))
                .map(DateAttendee::getStatus)
                .orElse(JoinDateStatus.NOT_REQUESTED);
        return new DateAttendeeStatusResponse(joinDateStatus, currentUserId.toString(), dateId);
    }

    public DateAttendeeStatusResponse rejectDateAttendeeRequest(String dateId, String attendeeId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        rejectAttendee(dateId, attendeeId, currentUser);
        return new DateAttendeeStatusResponse(JoinDateStatus.REJECTED, attendeeId, dateId);
    }

    private void rejectAttendee(String dateId, String attendeeId, UUID currentUser) {
        dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), UUID.fromString(attendeeId)))
                .ifPresentOrElse(
                        attendee -> {
                            if (!attendee.getId().getAttendeeId().equals(currentUser)) {
                                attendee.setStatus(JoinDateStatus.REJECTED);
                                dateAttendeeRepository.save(attendee);
                            }
                        },
                        () -> {
                            throw new AttendeeNotFoundException("Attendee not found for date: " + dateId);
                        });
    }

    @Transactional
    public void deleteAllAttendees(Date date) {
        dateAttendeeRepository.deleteAllByIdDateId(date.getId());
    }
}
