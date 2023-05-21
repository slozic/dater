package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET})
public class DatesController {

    private final DateRepository dateRepository;
    private final DateAttendeeRepository dateAttendeeRepository;

    @GetMapping
    public List<DateEventDto> getAllDates() throws UnauthorizedException {
        final UUID currentUser = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        final List<Date> dateList = dateRepository.findAll();
        return dateList.stream()
                       .filter(date -> !date.getCreatedBy().equals(currentUser))
                       .map(date -> new DateEventDto(date.getId().toString(), date.getTitle(), date.getLocation(),
                                                     date.getDescription(), date.getUser().getUsername(), "", date.getScheduledTime().toString(),
                       ""))
                       .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DateEventDto getDateById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        final Date dateEvent = dateRepository.findById(UUID.fromString(dateId)).orElseGet(Date::new);
        final Optional<DateAttendee> optionalDateAttendee = dateAttendeeRepository.findOneByAttendeeIdAndDateId(JwtAuthenticatedUserService.getCurrentUserOrThrow(), UUID.fromString(dateId));
        return List.of(dateEvent).stream()
                       .map(date -> new DateEventDto(
                               date.getId().toString(),
                               date.getTitle(),
                               date.getLocation(),
                               date.getDescription(),
                               date.getUser().getUsername(),
                               "",
                               date.getScheduledTime().toString(), getJoinDateStatus(optionalDateAttendee).toString()

                       ))
                       .collect(Collectors.toList()).stream().findFirst().get();
    }

    private JoinDateStatus getJoinDateStatus(final Optional<DateAttendee> optionalDateAttendee) {
        if(optionalDateAttendee.isEmpty()){
            return JoinDateStatus.AVAILABLE;
        }
        if (optionalDateAttendee.isPresent() & optionalDateAttendee.get().getAccepted()) {
            return JoinDateStatus.ACCEPTED;
        } else if (!optionalDateAttendee.get().getAccepted()) {
            return JoinDateStatus.PENDING;
        }
        return JoinDateStatus.AVAILABLE;
    }

    @PostMapping
    public Date createDateEvent(@RequestBody final CreateDateEventRequest request) throws UnauthorizedException {
        Date date = Date.builder()
                        .title(request.title())
                        .description(request.description())
                        .location(request.location())
                        .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(request.scheduledTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                        .createdBy(JwtAuthenticatedUserService.getCurrentUserOrThrow())
                        .build();
        final Date dateCreated = dateRepository.save(date);
        DateAttendee dateAttendee = DateAttendee.builder()
                                                .dateId(dateCreated.getId())
                                                .attendeeId(dateCreated.getCreatedBy())
                                                .accepted(true)
                                                .build();
        dateAttendeeRepository.save(dateAttendee);
        return dateCreated;
    }

    @GetMapping("/user/date")
    public List<MyDateEventDto> getDatesByCurrentUser() throws UnauthorizedException {
        final UUID currentUser = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        final List<DateAttendee> dateList = dateRepository.findAllCreatedByUserAndRequestedByUser(currentUser);
        return dateList.stream()
                       .map(dateAttendee -> new MyDateEventDto(dateAttendee.getDateId().toString(), dateAttendee.getDate().getTitle(), dateAttendee.getDate().getLocation(),
                                                     dateAttendee.getDate().getDescription(), dateAttendee.getUser().getUsername(), "", dateAttendee.getDate().getScheduledTime().toString(),
                                                     "", dateAttendee.getDate().getCreatedBy().equals(currentUser)))
                       .collect(Collectors.toList());
    }

}
