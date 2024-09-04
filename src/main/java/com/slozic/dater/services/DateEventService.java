package com.slozic.dater.services;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateEventService {
    private final DateEventRepository dateEventRepository;
    private final DateAttendeeRepository dateAttendeeRepository;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final DateImageService dateImageService;

    @Transactional(readOnly = true)
    public List<DateEventDto> getDateEventDtos() {
        final List<Date> dateList = dateEventRepository.findAll();

        return dateList.stream()
                .map(date -> new DateEventDto(
                        date.getId().toString(),
                        date.getTitle(),
                        date.getLocation(),
                        date.getDescription(),
                        date.getUser().getUsername(), "",
                        date.getScheduledTime().toString(),
                        ""))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DateEventDto getDateEventDto(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateEventRepository.findById(UUID.fromString(dateId)).orElseGet(Date::new);
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final Optional<DateAttendee> optionalDateAttendee = dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUser, UUID.fromString(dateId));

        return List.of(dateEvent).stream()
                .map(date -> new DateEventDto(
                        date.getId().toString(),
                        date.getTitle(),
                        date.getLocation(),
                        date.getDescription(),
                        date.getUser().getUsername(),
                        "",
                        date.getScheduledTime().toString(),
                        getJoinDateStatus(optionalDateAttendee).toString()

                ))
                .collect(Collectors.toList()).stream().findFirst().get();
    }

    private JoinDateStatus getJoinDateStatus(final Optional<DateAttendee> optionalDateAttendee) {
        if (optionalDateAttendee.isPresent()) {
            if (optionalDateAttendee.get().getAccepted())
                return JoinDateStatus.ACCEPTED;
            else
                return JoinDateStatus.PENDING;
        }
        return JoinDateStatus.AVAILABLE;
    }

    @Transactional
    public UUID createDateEventFromRequest(final CreateDateEventRequest request) {
        try {
            final Date dateCreated = createDateEvent(request);
            createDefaultDateAttendee(dateCreated);
            dateImageService.createDateEventImage(request.image(), dateCreated);
            return dateCreated.getId();
        } catch (Exception e) {
            if (e instanceof UnauthorizedException ue) {
                throw ue;
            }
            String userId = jwtAuthenticatedUserService.getCurrentUserOrThrow().toString();
            log.error("Failed to create date event for user with id {} and title {}", userId, request.title());
            throw new DateEventException(e.getCause());
        }
    }

    private Date createDateEvent(final CreateDateEventRequest request) throws UnauthorizedException {
        Date date = Date.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(request.scheduledTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                .createdBy(UUID.fromString(request.dateCreator()))
                .build();
        final Date dateCreated = dateEventRepository.save(date);
        return dateCreated;
    }

    private DateAttendee createDefaultDateAttendee(Date dateCreated) {
        DateAttendee dateAttendee = DateAttendee.builder()
                .dateId(dateCreated.getId())
                .attendeeId(dateCreated.getCreatedBy())
                .accepted(true)
                .build();
        return dateAttendeeRepository.save(dateAttendee);
    }

}
