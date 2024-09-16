package com.slozic.dater.services;

import com.slozic.dater.controllers.params.DateQueryParameters;
import com.slozic.dater.dto.enums.DateFilter;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.dto.response.dates.DateEventListData;
import com.slozic.dater.dto.response.dates.DateEventListResponse;
import com.slozic.dater.dto.response.dates.DateEventResponse;
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
import java.util.ArrayList;
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
    public DateEventListResponse getDateEvents(DateQueryParameters dateQueryParameters, UUID currentUser) {
        List<Date> dateList = filterDatesByParameters(dateQueryParameters, currentUser);
        return mapToListResponse(dateList);
    }
    
    private List<Date> filterDatesByParameters(DateQueryParameters dateQueryParameters, UUID currentUser) {
        List<Date> dateList = new ArrayList<>();
        DateFilter dateFilter = DateFilter.fromString(dateQueryParameters.filter());

        if (dateFilter.equals(DateFilter.ALL)) {
            dateList = dateEventRepository.findAll();
        } else {
            if (dateFilter.equals(DateFilter.OWNED)) {
                dateList = dateEventRepository.findAllByCreatedBy(currentUser);
            } else if (dateFilter.equals(DateFilter.REQUESTED)) {
                dateList = dateEventRepository.findDatesByAttendeeId(currentUser);
            }
        }
        return dateList;
    }

    private DateEventListResponse mapToListResponse(List<Date> dateList) {
        List<DateEventListData> dateEventList = dateList.stream()
                .map(mapEntityToListDto())
                .collect(Collectors.toList());
        return new DateEventListResponse(dateEventList);
    }

    private Function<Date, DateEventListData> mapEntityToListDto() {
        return date -> new DateEventListData(
                date.getId().toString(),
                date.getTitle(),
                date.getLocation(),
                date.getDescription(),
                date.getScheduledTime().toString());
    }

    @Transactional(readOnly = true)
    public DateEventResponse getDateEvent(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId))
                .orElseThrow(() -> new DateEventException("No date event found: " + dateId));
        return mapEntityToDto().apply(dateEvent);
    }

    private Function<Date, DateEventResponse> mapEntityToDto() {
        return date -> new DateEventResponse(
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

    @Transactional
    public DateEventCreatedResponse createDateEventWithDefaultAttendee(final CreateDateEventRequest request, String userId) {
        final Date dateCreated = saveDateEvent(request, userId);
        dateAttendeesService.createDefaultDateAttendee(dateCreated);
        return new DateEventCreatedResponse(dateCreated.getId().toString());
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
