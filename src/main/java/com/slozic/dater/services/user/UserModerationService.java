package com.slozic.dater.services.user;

import com.slozic.dater.dto.enums.UserReportReason;
import com.slozic.dater.dto.request.ReportUserRequest;
import com.slozic.dater.dto.response.UserModerationActionResponse;
import com.slozic.dater.exceptions.user.UserBlockedException;
import com.slozic.dater.exceptions.user.UserNotFoundException;
import com.slozic.dater.models.UserBlock;
import com.slozic.dater.models.UserReport;
import com.slozic.dater.repositories.UserBlockRepository;
import com.slozic.dater.repositories.UserReportRepository;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserModerationService {
    private static final int DUPLICATE_REPORT_COOLDOWN_MINUTES = 15;

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserReportRepository userReportRepository;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Transactional
    public UserModerationActionResponse blockUser(final String blockedUserId) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final UUID parsedBlockedUserId = parseAndValidateTargetUser(blockedUserId, currentUserId);
        ensureBlockExists(currentUserId, parsedBlockedUserId);
        return new UserModerationActionResponse(parsedBlockedUserId.toString(), false, true);
    }

    @Transactional
    public UserModerationActionResponse reportUser(final String reportedUserId, final ReportUserRequest request) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final UUID parsedReportedUserId = parseAndValidateTargetUser(reportedUserId, currentUserId);
        createReport(currentUserId, parsedReportedUserId, request);
        return new UserModerationActionResponse(parsedReportedUserId.toString(), true, false);
    }

    @Transactional
    public UserModerationActionResponse reportAndBlockUser(final String targetUserId, final ReportUserRequest request) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final UUID parsedTargetUserId = parseAndValidateTargetUser(targetUserId, currentUserId);
        createReport(currentUserId, parsedTargetUserId, request);
        ensureBlockExists(currentUserId, parsedTargetUserId);
        return new UserModerationActionResponse(parsedTargetUserId.toString(), true, true);
    }

    @Transactional(readOnly = true)
    public boolean areUsersBlocked(final UUID firstUserId, final UUID secondUserId) {
        if (firstUserId == null || secondUserId == null || firstUserId.equals(secondUserId)) {
            return false;
        }
        return userBlockRepository.existsBlockBetween(firstUserId, secondUserId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> findBlockedUserIdsForUser(
            final UUID currentUserId,
            final Collection<UUID> candidateUserIds
    ) {
        if (currentUserId == null || candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Set.of();
        }
        final Set<UUID> filteredCandidateIds = candidateUserIds.stream()
                .filter(candidateId -> candidateId != null && !candidateId.equals(currentUserId))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (filteredCandidateIds.isEmpty()) {
            return Set.of();
        }
        return userBlockRepository.findBlockedUserIdsForUser(currentUserId, filteredCandidateIds);
    }

    @Transactional(readOnly = true)
    public void assertUsersNotBlocked(final UUID firstUserId, final UUID secondUserId, final String detailMessage) {
        if (areUsersBlocked(firstUserId, secondUserId)) {
            throw new UserBlockedException(detailMessage);
        }
    }

    private UUID parseAndValidateTargetUser(final String targetUserId, final UUID currentUserId) {
        final UUID parsedTargetUserId = UUID.fromString(targetUserId);
        if (parsedTargetUserId.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot moderate your own user.");
        }
        userRepository.findOneById(parsedTargetUserId)
                .orElseThrow(() -> new UserNotFoundException("User with id not found: " + targetUserId));
        return parsedTargetUserId;
    }

    private void ensureBlockExists(final UUID blockerUserId, final UUID blockedUserId) {
        final boolean alreadyBlocked = userBlockRepository.existsByBlockerIdAndBlockedId(blockerUserId, blockedUserId);
        if (alreadyBlocked) {
            return;
        }
        final UserBlock userBlock = UserBlock.builder()
                .blockerId(blockerUserId)
                .blockedId(blockedUserId)
                .build();
        userBlockRepository.save(userBlock);
    }

    private void createReport(final UUID reporterId, final UUID reportedId, final ReportUserRequest request) {
        final UserReportReason reason = UserReportReason.fromString(request.reason());
        final OffsetDateTime duplicateCutoff = OffsetDateTime.now().minusMinutes(DUPLICATE_REPORT_COOLDOWN_MINUTES);
        final boolean duplicateReportExists = userReportRepository.existsByReporterIdAndReportedIdAndReasonAndCreatedAtAfter(
                reporterId,
                reportedId,
                reason,
                duplicateCutoff
        );
        if (duplicateReportExists) {
            throw new IllegalArgumentException("You have already reported this user for this reason recently.");
        }
        final String normalizedNote = request.note() == null || request.note().isBlank()
                ? null
                : request.note().trim();
        final UserReport userReport = UserReport.builder()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .reason(reason)
                .note(normalizedNote)
                .build();
        userReportRepository.save(userReport);
    }
}
