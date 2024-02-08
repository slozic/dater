package com.slozic.dater.services;

import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DateAttendeeServiceTest {

    @InjectMocks
    private DateAttendeesService dateAttendeesService;

    @Mock
    private DateAttendeeRepository dateAttendeeRepository;

    @Test
    public void acceptAttendeeRequest_shouldWorkWithSuccess() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUser = UUID.randomUUID();

        Optional<DateAttendee> optionalDateAttendee = Optional.of(DateAttendee.builder()
                .attendeeId(userId)
                .dateId(dateId)
                .build());
        when(dateAttendeeRepository.findOneByAttendeeIdAndDateId(userId, dateId)).thenReturn(optionalDateAttendee);

        // when
        dateAttendeesService.acceptAttendeeRequest(dateId.toString(), userId.toString(), currentUser);

        // then
        Mockito.verify(dateAttendeeRepository, times(1)).save(optionalDateAttendee.get());
        assertThat(optionalDateAttendee.get().getAccepted()).isTrue();
    }

    @Test
    public void acceptAttendeeRequest_shouldThrowExceptionOnNonExistingUser() {
        // given
        UUID dateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUser = UUID.randomUUID();

        when(dateAttendeeRepository.findOneByAttendeeIdAndDateId(userId, dateId)).thenReturn(Optional.empty());

        // when
        assertThrows(IllegalArgumentException.class,
                () -> dateAttendeesService.acceptAttendeeRequest(
                        dateId.toString(), userId.toString(), currentUser)
        );

        // then
        Mockito.verify(dateAttendeeRepository, times(0)).save(any(DateAttendee.class));
    }
}
