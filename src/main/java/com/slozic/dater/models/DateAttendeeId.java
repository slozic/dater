package com.slozic.dater.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DateAttendeeId implements Serializable {
    private UUID dateId;
    private UUID attendeeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }

        DateAttendeeId dateAttendeeId = (DateAttendeeId) o;
        return Objects.equals(attendeeId, dateAttendeeId.attendeeId) && Objects.equals(dateId, dateAttendeeId.dateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendeeId, dateId);
    }
}