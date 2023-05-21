package com.slozic.dater.security;

import com.slozic.dater.exceptions.UnauthorizedException;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Extracts information from currently authenticated user.
 */
@Service
@Slf4j
@AllArgsConstructor
public class JwtAuthenticatedUserService {

    public static UUID getCurrentUserOrThrow() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("Authentication object could not be obtained");
            throw new UnauthorizedException();
        }

        String userId = (String) authentication.getPrincipal();

        if (StringUtils.isBlank(userId)) {
            log.error("Unexpected userId value: {}", userId);
            throw new UnauthorizedException();
        }
        return UUID.fromString(userId);
    }
}
