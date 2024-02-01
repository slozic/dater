package com.slozic.dater.services;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateAttendeesService {

    private final DateAttendeeRepository dateAttendeeRepository;

    public List<DateAttendeeDto> getDateAttendeeDtos(String dateId) {
        final List<DateAttendee> dateAttendeesList = dateAttendeeRepository.findAllByDateId(UUID.fromString(dateId));
        return dateAttendeesList.stream()
                .filter(attendee -> !attendee.getAttendeeId().equals(attendee.getDate().getCreatedBy()))
                .map(dateAttendee -> DateAttendeeDto.from(dateAttendee))
                .collect(Collectors.toList());
    }

    public void addAttendeeRequest(String dateId, UUID currentUserId) {
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUserId, UUID.fromString(dateId))
                .ifPresentOrElse(attendee -> {
                            throw new IllegalArgumentException("User already requested to join!");
                        },
                        () -> {
                            dateAttendeeRepository.save(DateAttendee.builder()
                                    .dateId(UUID.fromString(dateId))
                                    .attendeeId(currentUserId)
                                    .build());
                        });
    }

    public void acceptAttendeeRequest(String dateId, String userId, UUID currentUser) {
        dateAttendeeRepository.findOneByAttendeeIdAndDateId(UUID.fromString(userId), UUID.fromString(dateId))
                .ifPresentOrElse(attendee -> {
                            if (!attendee.getDate().getCreatedBy().equals(currentUser)) {
                                throw new IllegalArgumentException("Date request can only be accepted by user who created it!");
                            }
                            attendee.setAccepted(true);
                            dateAttendeeRepository.save(attendee);
                        },
                        () -> {
                            throw new IllegalArgumentException("User could not be found!");
                        });
    }

}
