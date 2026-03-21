package com.slozic.dater.services.attendees;

import com.slozic.dater.dto.DateAttendeeDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.response.attendees.DateAttendeeResponse;
import com.slozic.dater.dto.response.attendees.DateAttendeeStatusResponse;
import com.slozic.dater.exceptions.attendee.AttendeeAlreadyExistsException;
import com.slozic.dater.exceptions.attendee.AttendeeNotFoundException;
import com.slozic.dater.exceptions.dateevent.DateEventAccessPermissionException;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateAttendeesService {
    private final DateAttendeeRepository dateAttendeeRepository;
    private final DateEventRepository dateEventRepository;
    private final UserRepository userRepository;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final NotificationService notificationService;

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
        final Date date = dateEventRepository.findById(UUID.fromString(dateId)).orElseThrow(() ->
                new DateEventException("Date event not found: " + dateId));
        createNewDateAttendee(dateId, currentUserId);
        notifyDateOwnerAboutNewRequestSafely(date, currentUserId);
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

    private void notifyDateOwnerAboutNewRequest(final Date date, final UUID requesterUserId) {
        if (requesterUserId.equals(date.getCreatedBy())) {
            return;
        }
        final String requesterUsername = userRepository.findOneById(requesterUserId)
                .map(com.slozic.dater.models.User::getUsername)
                .orElse("Someone");
        notificationService.notifyDateRequestReceived(
                date.getCreatedBy(),
                date.getId(),
                date.getTitle(),
                requesterUsername
        );
    }

    @Transactional
    public DateAttendeeStatusResponse acceptAttendeeRequest(String dateId, String userId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final Date date = getDateIfOwnerOrThrow(dateId, currentUser);
        final UUID parsedDateId = date.getId();
        final UUID acceptedUserId = UUID.fromString(userId);
        if (acceptedUserId.equals(date.getCreatedBy())) {
            throw new DateEventException("Date owner cannot be accepted as attendee.");
        }
        final boolean changedToAccepted = acceptDateAttendee(parsedDateId, acceptedUserId, currentUser);
        if (changedToAccepted) {
            notifyAttendeeAcceptedSafely(acceptedUserId, parsedDateId, date.getTitle());
        }
        return new DateAttendeeStatusResponse(JoinDateStatus.ACCEPTED, userId, dateId);
    }

    private void notifyDateOwnerAboutNewRequestSafely(final Date date, final UUID requesterUserId) {
        try {
            notifyDateOwnerAboutNewRequest(date, requesterUserId);
        } catch (RuntimeException ex) {
            log.warn(
                    "Date request notification failed for dateId={}, requesterUserId={}, reason={}",
                    date.getId(),
                    requesterUserId,
                    ex.getMessage()
            );
        }
    }

    private void notifyAttendeeAcceptedSafely(final UUID acceptedUserId, final UUID dateId, final String dateTitle) {
        try {
            notificationService.notifyAttendeeAccepted(acceptedUserId, dateId, dateTitle);
        } catch (RuntimeException ex) {
            log.warn(
                    "Attendee accepted notification failed for dateId={}, acceptedUserId={}, reason={}",
                    dateId,
                    acceptedUserId,
                    ex.getMessage()
            );
        }
    }

    private boolean acceptDateAttendee(UUID dateId, UUID userId, UUID currentUser) {
        final DateAttendee attendee = dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, userId))
                .orElseThrow(() -> new AttendeeNotFoundException("Attendee not found for date: " + dateId));
        if (attendee.getId().getAttendeeId().equals(currentUser)) {
            return false;
        }
        final boolean changedToAccepted = attendee.getStatus() != JoinDateStatus.ACCEPTED;
        attendee.setStatus(JoinDateStatus.ACCEPTED);
        dateAttendeeRepository.save(attendee);
        return changedToAccepted;
    }

    public DateAttendeeStatusResponse getDateAttendeeStatus(String dateId, UUID currentUserId) {
        JoinDateStatus joinDateStatus = dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), currentUserId))
                .map(DateAttendee::getStatus)
                .orElse(JoinDateStatus.NOT_REQUESTED);
        return new DateAttendeeStatusResponse(joinDateStatus, currentUserId.toString(), dateId);
    }

    public DateAttendeeStatusResponse rejectDateAttendeeRequest(String dateId, String attendeeId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final Date date = getDateIfOwnerOrThrow(dateId, currentUser);
        final UUID attendeeUuid = UUID.fromString(attendeeId);
        if (attendeeUuid.equals(date.getCreatedBy())) {
            throw new DateEventException("Date owner cannot be rejected as attendee.");
        }
        rejectAttendee(date.getId(), attendeeUuid);
        return new DateAttendeeStatusResponse(JoinDateStatus.REJECTED, attendeeId, dateId);
    }

    @Transactional
    public DateAttendeeStatusResponse cancelMyRequest(String dateId, UUID currentUserId) {
        DateAttendee attendee = dateAttendeeRepository.findOneById(new DateAttendeeId(UUID.fromString(dateId), currentUserId))
                .orElseThrow(() -> new AttendeeNotFoundException("Attendee not found for date: " + dateId));

        if (!JoinDateStatus.ON_WAITLIST.equals(attendee.getStatus())) {
            throw new DateEventException("Cannot cancel request with status: " + attendee.getStatus());
        }

        dateAttendeeRepository.delete(attendee);
        return new DateAttendeeStatusResponse(JoinDateStatus.NOT_REQUESTED, currentUserId.toString(), dateId);
    }

    private Date getDateIfOwnerOrThrow(final String dateId, final UUID currentUser) {
        final Date date = dateEventRepository.findById(UUID.fromString(dateId))
                .orElseThrow(() -> new DateEventException("Date event not found: " + dateId));
        if (!currentUser.equals(date.getCreatedBy())) {
            throw new DateEventAccessPermissionException(
                    "User does not have permission to manage attendees for date: " + dateId
            );
        }
        return date;
    }

    private void rejectAttendee(final UUID dateId, final UUID attendeeId) {
        dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, attendeeId))
                .ifPresentOrElse(
                        attendee -> {
                            attendee.setStatus(JoinDateStatus.REJECTED);
                            dateAttendeeRepository.save(attendee);
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
