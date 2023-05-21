package com.slozic.dater.repositories;

import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DateRepository extends JpaRepository<Date, UUID> {
    @Query(nativeQuery = false, value = " select da from DateAttendee da " +
                                        " left join fetch da.user u " +
                                        " left join fetch da.date d " +
                                        " where da.attendeeId = :id ")
    List<DateAttendee> findAllCreatedByUserAndRequestedByUser(UUID id);
}
