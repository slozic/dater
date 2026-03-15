package com.slozic.dater.services;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UpdateUserProfileRequest;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
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
}
