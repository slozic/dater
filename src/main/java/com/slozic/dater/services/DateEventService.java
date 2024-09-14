package com.slozic.dater.services;

import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.DateEventData;
import com.slozic.dater.dto.response.DateEventResponse;
import com.slozic.dater.exceptions.DateEventException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateEventService {
    private final DateEventRepository dateEventRepository;
    private final DateAttendeesService dateAttendeesService;

    @Transactional(readOnly = true)
    public DateEventResponse getDateEvents() {
        final List<Date> dateList = dateEventRepository.findAll();
        return mapToResponse(dateList);
    }

    private DateEventResponse mapToResponse(List<Date> dateList) {
        List<DateEventData> dateEventList = dateList.stream()
                .map(mapEntityToDto())
                .collect(Collectors.toList());
        return new DateEventResponse(dateEventList);
    }

    private Function<Date, DateEventData> mapEntityToDto() {
        return date -> new DateEventData(
                date.getId().toString(),
                date.getTitle(),
                date.getLocation(),
                date.getDescription(),
                date.getUser().getUsername(),
                date.getCreatedBy().toString(),
                "",
                date.getScheduledTime().toString(),
                "");
    }

    @Transactional(readOnly = true)
    public DateEventData getDateEventDto(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId))
                .orElseThrow(() -> new DateEventException("No date event found: " + dateId));
        return mapEntityToDto().apply(dateEvent);
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
        return dateEventRepository.save(date);
    }


}
