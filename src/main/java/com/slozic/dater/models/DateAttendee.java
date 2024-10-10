package com.slozic.dater.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "date_attendees")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class DateAttendee {
    @EmbeddedId
    private DateAttendeeId id;

    @NotNull
    @Builder.Default
    private Boolean accepted = false;

    @Builder.Default
    private Boolean softDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendeeId", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dateId", nullable = false, insertable = false, updatable = false)
    private Date date;

}
