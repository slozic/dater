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
@IdClass(DateAttendeeId.class)
public class DateAttendee {
    @Id
    private UUID attendeeId;

    @Id
    private UUID dateId;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final DateAttendee dateAttendee = (DateAttendee) o;
        return Objects.equals(attendeeId, dateAttendee.attendeeId) && Objects.equals(dateId, dateAttendee.dateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendeeId,dateId);
    }

}
