package com.slozic.dater.services;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateEventService {
    private final DateEventRepository dateEventRepository;
    private final DateAttendeesService dateAttendeesService;

    private final LocalImageStorageService localImageStorageService;

    private final DateImageDBService dateImageDBService;

    @Transactional(readOnly = true)
    public List<DateEventDto> getDateEventDtos() {
        final List<Date> dateList = dateEventRepository.findAll();

        return dateList.stream()
                .map(date -> new DateEventDto(
                        date.getId().toString(),
                        date.getTitle(),
                        date.getLocation(),
                        date.getDescription(),
                        date.getUser().getUsername(),
                        "",
                        date.getScheduledTime().toString(),
                        ""))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DateEventDto getDateEventDto(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId)).orElseGet(Date::new);
        return new DateEventDto(
                dateEvent.getId().toString(),
                dateEvent.getTitle(),
                dateEvent.getLocation(),
                dateEvent.getDescription(),
                dateEvent.getUser().getUsername(),
                "",
                dateEvent.getScheduledTime().toString(),
                "");
    }

    @Transactional
    public UUID createDateEvent(final CreateDateEventRequest request, String userId) {
        final Date dateCreated = saveDateEvent(request, userId);
        dateAttendeesService.createDefaultDateAttendee(dateCreated);
        return dateCreated.getId();
    }

    private Date saveDateEvent(final CreateDateEventRequest request, String userId) {
        Date date = Date.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(request.scheduledTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                .createdBy(UUID.fromString(userId))
                .build();
        final Date dateCreated = dateEventRepository.save(date);
        return dateCreated;
    }


}
