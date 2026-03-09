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

    private final ObjectMapper objectMapper;

    public void sendPush(final String expoPushToken, final String title, final String body, final String dateId) {
        if (expoPushToken == null || expoPushToken.isBlank()) {
            return;
        }
        try {
            final Map<String, Object> payload = Map.of(
                    "to", expoPushToken,
                    "title", title,
                    "body", body,
                    "data", Map.of("dateId", dateId == null ? "" : dateId),
                    "sound", "default"
            );
            final String json = objectMapper.writeValueAsString(List.of(payload));
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EXPO_PUSH_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ex) {
            log.warn("Failed to send push notification to Expo token: {}", ex.getMessage());
        }
    }
}
