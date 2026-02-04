package com.slozic.dater.models;

import jakarta.persistence.*;
import com.slozic.dater.dto.enums.JoinDateStatus;
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
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JoinDateStatus status = JoinDateStatus.ON_WAITLIST;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendeeId", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dateId", nullable = false, insertable = false, updatable = false)
    private Date date;

}
