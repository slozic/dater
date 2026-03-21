package com.slozic.dater.services;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UpdateUserProfileRequest;
import com.slozic.dater.dto.request.UserRegistrationRequest;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Test
    void updateCurrentUser_shouldUpdateNotificationPreferences() throws Exception {
        final UUID userId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .username("guest")
                .email("guest@example.com")
                .attendeeAcceptedNotificationsEnabled(true)
                .dateRequestNotificationsEnabled(true)
                .chatMessageNotificationsEnabled(true)
                .build();
        when(jwtAuthenticatedUserService.getCurrentUserOrThrow()).thenReturn(userId);
        when(userRepository.findOneById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                true
        );

        final UserDto updated = userService.updateCurrentUser(request);

        assertThat(updated.attendeeAcceptedNotificationsEnabled()).isFalse();
        assertThat(updated.dateRequestNotificationsEnabled()).isFalse();
        assertThat(updated.chatMessageNotificationsEnabled()).isTrue();
        assertThat(user.isAttendeeAcceptedNotificationsEnabled()).isFalse();
        assertThat(user.isDateRequestNotificationsEnabled()).isFalse();
        assertThat(user.isChatMessageNotificationsEnabled()).isTrue();
    }

    @Test
    void doUserRegistration_shouldFailWhenUsernameAlreadyExists() {
        final UserRegistrationRequest request = new UserRegistrationRequest(
                "John",
                "Doe",
                "existing-user",
                "password",
                "john@example.com",
                "1990-01-01",
                "MALE"
        );
        when(userRepository.findOneByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.findOneByUsername("existing-user"))
                .thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).username("existing-user").build()));

        assertThrows(IllegalArgumentException.class, () -> userService.doUserRegistration(request));
    }
}
