package com.slozic.dater.repositories;

import com.slozic.dater.dto.enums.UserReportReason;
import com.slozic.dater.models.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReport, UUID> {
    boolean existsByReporterIdAndReportedIdAndReasonAndCreatedAtAfter(
            UUID reporterId,
            UUID reportedId,
            UserReportReason reason,
            OffsetDateTime createdAt
    );
}
