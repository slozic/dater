package com.slozic.dater.services.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationDeliveryService {
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final ObjectMapper PUSH_OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;

    public void sendPush(
            final String expoPushToken,
            final String title,
            final String body,
            final String dateId,
            final String notificationType
    ) {
        if (expoPushToken == null || expoPushToken.isBlank()) {
            log.debug("Push skipped: missing Expo token.");
            return;
        }
        try {
            final Map<String, Object> payload = Map.of(
                    "to", expoPushToken,
                    "title", title,
                    "body", body,
                    "data", Map.of(
                            "dateId", dateId == null ? "" : dateId,
                            "notificationType", notificationType == null ? "" : notificationType
                    ),
                    "sound", "default"
            );
            final String json = PUSH_OBJECT_MAPPER.writeValueAsString(List.of(payload));
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EXPO_PUSH_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final String responseBody = response.body() == null ? "" : response.body();
            log.debug(
                    "Push send attempted. status={}, tokenSuffix={}, response={}",
                    response.statusCode(),
                    expoPushToken.length() > 8 ? expoPushToken.substring(expoPushToken.length() - 8) : expoPushToken,
                    responseBody
            );
        } catch (Exception ex) {
            log.warn("Failed to send push notification to Expo token: {}", ex.getMessage());
        }
    }
}
