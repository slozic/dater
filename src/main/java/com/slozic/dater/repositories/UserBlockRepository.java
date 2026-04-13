package com.slozic.dater.repositories;

import com.slozic.dater.models.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("""
            SELECT (COUNT(ub) > 0)
            FROM UserBlock ub
            WHERE (ub.blockerId = :firstUserId AND ub.blockedId = :secondUserId)
               OR (ub.blockerId = :secondUserId AND ub.blockedId = :firstUserId)
            """)
    boolean existsBlockBetween(@Param("firstUserId") UUID firstUserId, @Param("secondUserId") UUID secondUserId);

    @Query("""
            SELECT DISTINCT
                CASE
                    WHEN ub.blockerId = :currentUserId THEN ub.blockedId
                    ELSE ub.blockerId
                END
            FROM UserBlock ub
            WHERE (ub.blockerId = :currentUserId AND ub.blockedId IN :candidateUserIds)
               OR (ub.blockedId = :currentUserId AND ub.blockerId IN :candidateUserIds)
            """)
    Set<UUID> findBlockedUserIdsForUser(
            @Param("currentUserId") UUID currentUserId,
            @Param("candidateUserIds") Collection<UUID> candidateUserIds
    );
}
