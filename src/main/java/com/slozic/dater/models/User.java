package com.slozic.dater.models;

import com.slozic.dater.dto.request.UserRegistrationRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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

    private String gender;

    private String dateListGenderFilter;

    @NotNull
    private OffsetDateTime createdAt;

    private boolean enabled;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static User fromUserRegistrationRequest(final UserRegistrationRequest request) {
        return User.builder()
                .username(request.username())
                .firstname(request.firstName())
                .lastname(request.lastName())
                .email(request.email())
                .birthday(LocalDate.parse(request.birthday(), DateTimeFormatter.ISO_LOCAL_DATE))
                .gender(request.gender())
                .dateListGenderFilter("ALL")
                .enabled(true)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
