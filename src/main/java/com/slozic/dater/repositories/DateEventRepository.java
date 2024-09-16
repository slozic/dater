package com.slozic.dater.repositories;

import com.slozic.dater.models.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DateEventRepository extends JpaRepository<Date, UUID> {
    List<Date> findAllByCreatedBy(UUID id);

    @Query(" SELECT d FROM DateAttendee da " +
            " JOIN da.date d " +
            " WHERE da.user.id = :attendeeId " +
            " AND d.createdBy != :attendeeId ")
    List<Date> findDatesByAttendeeId(@Param("attendeeId") UUID attendeeId);
}
