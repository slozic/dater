package com.slozic.dater.services;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.response.DateAttendeeResponse;
import com.slozic.dater.exceptions.AttendeeAlreadyExistsException;
import com.slozic.dater.exceptions.AttendeeNotFoundException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateAttendeesService {
    private final DateAttendeeRepository dateAttendeeRepository;

    @Transactional
    public DateAttendeeResponse getAllDateAttendees(String dateId) {
        final List<DateAttendee> dateAttendeesList = dateAttendeeRepository.findAllByDateId(UUID.fromString(dateId));
        return getDateAttendeeResponse(dateAttendeesList);
    }

    private DateAttendeeResponse getDateAttendeeResponse(List<DateAttendee> dateAttendeesList) {
        List<DateAttendeeDto> dateAttendeeDtos = dateAttendeesList.stream()
                .map(dateAttendee -> DateAttendeeDto.from(dateAttendee))
                .collect(Collectors.toList());
        return new DateAttendeeResponse(dateAttendeeDtos);
    }

    public DateAttendee createDefaultDateAttendee(Date dateCreated) {
        DateAttendee dateAttendee = DateAttendee.builder()
                .dateId(dateCreated.getId())
                .attendeeId(dateCreated.getCreatedBy())
                .accepted(true)
                .build();
        return dateAttendeeRepository.save(dateAttendee);
    }

    @Transactional
    public JoinDateStatus addAttendeeToDate(String dateId, UUID currentUserId) {
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUserId, UUID.fromString(dateId))
                .ifPresentOrElse(
                        attendee -> {
                            throw new AttendeeAlreadyExistsException("Attendee already requested to join date: " + dateId);
                        },
                        () -> dateAttendeeRepository.save(DateAttendee.builder()
                                .dateId(UUID.fromString(dateId))
                                .attendeeId(currentUserId)
                                .build()));

        return JoinDateStatus.ON_WAITLIST;
    }

    @Transactional
    public JoinDateStatus acceptAttendeeRequest(String dateId, String userId, UUID currentUser) {
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(UUID.fromString(userId), UUID.fromString(dateId))
                .ifPresentOrElse(
                        attendee -> {
                            if (!attendee.getAttendeeId().equals(currentUser)) {
                                attendee.setAccepted(true);
                                dateAttendeeRepository.save(attendee);
                            }
                        },
                        () -> {
                            throw new AttendeeNotFoundException("Attendee not found for date: " + dateId);
                        });

        return JoinDateStatus.ACCEPTED;
    }

    public JoinDateStatus getDateAttendeeStatus(String dateId, UUID currentUserId) {
        return dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUserId, UUID.fromString(dateId))
                .map(dateAttendee -> dateAttendee.getAccepted() ? JoinDateStatus.ACCEPTED : JoinDateStatus.ON_WAITLIST)
                .orElseThrow(() -> new AttendeeNotFoundException("Attendee not found for date: " + dateId));
    }
}
