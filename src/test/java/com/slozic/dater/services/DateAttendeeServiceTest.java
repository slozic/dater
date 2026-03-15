package com.slozic.dater.services;

import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.exceptions.attendee.AttendeeNotFoundException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.attendees.DateAttendeesService;
import com.slozic.dater.services.notifications.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DateAttendeeServiceTest {
    @InjectMocks
    private DateAttendeesService dateAttendeesService;
    @Mock
    private DateAttendeeRepository dateAttendeeRepository;
    @Mock
    private DateEventRepository dateEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;
    @Mock
    private NotificationService notificationService;

    @Test
    public void acceptAttendeeRequest_shouldWorkWithSuccess() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUser = UUID.randomUUID();

        Optional<DateAttendee> optionalDateAttendee = Optional.of(DateAttendee.builder()
                .id(new DateAttendeeId(dateId, userId))
                .build());
        when(jwtAuthenticatedUserService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(Date.builder().id(dateId).title("Pool night").build()));
        when(dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, userId))).thenReturn(optionalDateAttendee);

        // when
        dateAttendeesService.acceptAttendeeRequest(dateId.toString(), userId.toString());

        // then
        Mockito.verify(dateAttendeeRepository, times(1)).save(optionalDateAttendee.get());
        verify(notificationService, times(1)).notifyAttendeeAccepted(eq(userId), eq(dateId), eq("Pool night"));
        assertThat(optionalDateAttendee.get().getStatus()).isEqualTo(JoinDateStatus.ACCEPTED);
    }

    @Test
    public void acceptAttendeeRequest_shouldThrowExceptionOnNonExistingUser() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUser = UUID.randomUUID();

        when(jwtAuthenticatedUserService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(Date.builder().id(dateId).title("Pool night").build()));
        when(dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, userId))).thenReturn(Optional.empty());

        // when
        assertThrows(AttendeeNotFoundException.class,
                () -> dateAttendeesService.acceptAttendeeRequest(
                        dateId.toString(), userId.toString())
        );

        // then
        Mockito.verify(dateAttendeeRepository, times(0)).save(any(DateAttendee.class));
        verify(notificationService, times(0)).notifyAttendeeAccepted(any(), any(), any());
    }

    @Test
    public void addAttendeeToDate_shouldNotifyDateOwnerOnNewRequest() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(dateEventRepository.findById(dateId))
                .thenReturn(Optional.of(Date.builder().id(dateId).createdBy(ownerId).title("Pool night").build()));
        when(dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, requesterId))).thenReturn(Optional.empty());
        when(userRepository.findOneById(requesterId))
                .thenReturn(Optional.of(com.slozic.dater.models.User.builder().id(requesterId).username("guest").build()));

        // when
        dateAttendeesService.addAttendeeToDate(dateId.toString(), requesterId);

        // then
        verify(notificationService, times(1))
                .notifyDateRequestReceived(eq(ownerId), eq(dateId), eq("Pool night"), eq("guest"));
    }

    @Test
    public void acceptAttendeeRequest_shouldNotFailWhenNotificationFails() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUser = UUID.randomUUID();

        Optional<DateAttendee> optionalDateAttendee = Optional.of(DateAttendee.builder()
                .id(new DateAttendeeId(dateId, userId))
                .build());
        when(jwtAuthenticatedUserService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(dateEventRepository.findById(dateId)).thenReturn(Optional.of(Date.builder().id(dateId).title("Pool night").build()));
        when(dateAttendeeRepository.findOneById(new DateAttendeeId(dateId, userId))).thenReturn(optionalDateAttendee);
        doThrow(new RuntimeException("notification-failure"))
                .when(notificationService).notifyAttendeeAccepted(userId, dateId, "Pool night");

        // when
        final var response = dateAttendeesService.acceptAttendeeRequest(dateId.toString(), userId.toString());

        // then
        assertThat(response.joinDateStatus()).isEqualTo(JoinDateStatus.ACCEPTED);
        verify(dateAttendeeRepository, times(1)).save(optionalDateAttendee.get());
        verify(notificationService, times(1)).notifyAttendeeAccepted(userId, dateId, "Pool night");
    }
}
