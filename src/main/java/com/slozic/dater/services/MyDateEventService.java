package com.slozic.dater.services;

import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDateEventService {
    private final DateAttendeeRepository dateAttendeeRepository;

    @Transactional(readOnly = true)
    public List<MyDateEventDto> getMyDateEventDtos(UUID currentUser) {
        final List<DateAttendee> dateList = dateAttendeeRepository.findAllCreatedByUserAndRequestedByUser(currentUser);
        //dateEventRepository.findAll();

        return dateList.stream()
                .map(dateAttendee -> new MyDateEventDto(
                        dateAttendee.getDateId().toString(),
                        dateAttendee.getDate().getTitle(),
                        dateAttendee.getDate().getLocation(),
                        dateAttendee.getDate().getDescription(),
                        dateAttendee.getUser().getUsername(), "",
                        dateAttendee.getDate().getScheduledTime().toString(),
                        dateAttendee.getDate().getCreatedBy().equals(currentUser)))
                .collect(Collectors.toList());
    }
}
