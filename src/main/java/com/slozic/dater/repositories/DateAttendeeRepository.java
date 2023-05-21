package com.slozic.dater.repositories;

import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DateAttendeeRepository extends JpaRepository<DateAttendee, DateAttendeeId> {
    List<DateAttendee> findAllByDateId(UUID dateId);
    Optional<DateAttendee> findOneByAttendeeIdAndDateId(UUID attendeeId, UUID dateId);
}
