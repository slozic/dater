package com.slozic.dater.services.dateevent;

import com.slozic.dater.controllers.params.DateQueryParameters;
import com.slozic.dater.dto.enums.DateFilter;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.request.UpdateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.dto.response.dates.DateEventListData;
import com.slozic.dater.dto.response.dates.DateEventListResponse;
import com.slozic.dater.dto.response.dates.DateEventResponse;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.exceptions.dateevent.DateEventNotFoundException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.attendees.DateAttendeesService;
import com.slozic.dater.services.images.DateEventImageService;
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
    private static final double DEFAULT_RADIUS_KM = 10.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private final DateEventRepository dateEventRepository;
    private final DateAttendeesService dateAttendeesService;
    private final DateEventImageService dateEventImageService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Transactional(readOnly = true)
    public DateEventListResponse getDateEvents(final DateQueryParameters dateQueryParameters, UUID currentUser) {
        List<Date> dateList = filterDatesByParameters(dateQueryParameters, currentUser);
        return mapToListResponse(dateList);
    }

    private List<Date> filterDatesByParameters(final DateQueryParameters dateQueryParameters, UUID currentUser) {
        List<Date> dateList = new ArrayList<>();
        DateFilter dateFilter = DateFilter.fromString(dateQueryParameters.filter());

        if (dateFilter.equals(DateFilter.ALL)) {
            dateList = dateEventRepository.findAllExcludingStatusForUser(currentUser, JoinDateStatus.REJECTED);
        } else {
            if (dateFilter.equals(DateFilter.OWNED)) {
                dateList = dateEventRepository.findAllByCreatedBy(currentUser);
            } else if (dateFilter.equals(DateFilter.REQUESTED)) {
                dateList = dateEventRepository.findDatesByAttendeeId(
                        currentUser,
                        List.of(JoinDateStatus.ON_WAITLIST, JoinDateStatus.ACCEPTED));
            }
        }
        return applyGeoFilter(dateList, dateQueryParameters);
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
                date.getLatitude(),
                date.getLongitude(),
                date.getDescription(),
                date.getScheduledTime().toString());
    }

    @Transactional(readOnly = true)
    public DateEventResponse getDateEvent(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId))
                .orElseThrow(() -> new DateEventNotFoundException("No date event found: " + dateId));
        return mapEntityToDto().apply(dateEvent);
    }

    private Function<Date, DateEventResponse> mapEntityToDto() {
        return date -> new DateEventResponse(
                date.getId().toString(),
                date.getTitle(),
                date.getLocation(),
                date.getLatitude(),
                date.getLongitude(),
                date.getDescription(),
                date.getUser().getUsername(),
                date.getCreatedBy().toString(),
                "",
                date.getScheduledTime().toString(),
                "");
    }

    @Transactional
    public DateEventCreatedResponse createDateEventWithDefaultAttendee(final CreateDateEventRequest request) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final Date dateCreated = saveDateEvent(request, currentUser.toString());
        dateAttendeesService.createDefaultDateAttendee(dateCreated);
        return new DateEventCreatedResponse(dateCreated.getId().toString());
    }

    private Date saveDateEvent(final CreateDateEventRequest request, String userId) {
        Date date = Date.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(request.scheduledTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                .createdBy(UUID.fromString(userId))
                .build();
        return dateEventRepository.save(date);
    }

    private List<Date> applyGeoFilter(final List<Date> dates, final DateQueryParameters dateQueryParameters) {
        if (dateQueryParameters.latitude().isEmpty() || dateQueryParameters.longitude().isEmpty()) {
            return dates;
        }
        final double latitude = dateQueryParameters.latitude().get();
        final double longitude = dateQueryParameters.longitude().get();
        final double radiusKm = dateQueryParameters.radiusKm().orElse(DEFAULT_RADIUS_KM);

        return dates.stream()
                .filter(date -> date.getLatitude() != null && date.getLongitude() != null)
                .filter(date -> haversineKm(latitude, longitude, date.getLatitude(), date.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double latDistance = Math.toRadians(lat2 - lat1);
        final double lonDistance = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Transactional
    public void deleteDateEvent(final String dateId) {
        Date dateEvent = validateUserDatePermissions(dateId);
        dateEventImageService.deleteAllImages(dateEvent);
        dateAttendeesService.deleteAllAttendees(dateEvent);
        dateEventRepository.deleteById(UUID.fromString(dateId));
    }

    @Transactional
    public DateEventResponse updateDateEvent(final String dateId, final UpdateDateEventRequest request) {
        Date dateEvent = validateUserDatePermissions(dateId);

        if (request.title() != null) {
            dateEvent.setTitle(request.title());
        }
        if (request.description() != null) {
            dateEvent.setDescription(request.description());
        }
        if (request.location() != null) {
            dateEvent.setLocation(request.location());
        }
        if (request.latitude() != null) {
            dateEvent.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            dateEvent.setLongitude(request.longitude());
        }
        if (request.scheduledTime() != null) {
            dateEvent.setScheduledTime(OffsetDateTime.of(
                    LocalDateTime.parse(request.scheduledTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    ZoneOffset.UTC));
        }

        Date updated = dateEventRepository.save(dateEvent);
        return mapEntityToDto().apply(updated);
    }

    private Date validateUserDatePermissions(String dateId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId)).orElseThrow(() -> new DateEventNotFoundException("Date event was not found: " + dateId));

        if(!dateEvent.getCreatedBy().equals(currentUser)){
            throw new DateEventAccessPermissionException("User does not have permission to delete the date event: " + dateId);
        }
        return dateEvent;
    }
}
