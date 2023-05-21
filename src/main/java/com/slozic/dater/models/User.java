package com.slozic.dater.models;

import com.slozic.dater.dto.request.UserRegistrationRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String firstname;

    private String lastname;

    @NotBlank
    private String email;

    private LocalDate birthday;

    private boolean enabled;

    public static User fromUserRegistrationRequest(final UserRegistrationRequest request) {
        return User.builder()
                   .username(request.username())
                   .firstname(request.firstName())
                   .lastname(request.lastName())
                   .email(request.email())
                   .birthday(LocalDate.parse(request.birthday(), DateTimeFormatter.ISO_LOCAL_DATE))
                   .enabled(true)
                   .build();
    }
}
