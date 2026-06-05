package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ScheduleRequestApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/schedule/requests";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(5));
    }

    // Врач — свои запросы
    public static JsonNode getMyRequests() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                baseRequest("/my").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    // Врач — создать запрос
    public static JsonNode createRequest(int dayOfWeek, String requestedStart,
                                         String requestedEnd, String reason) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("dayOfWeek", dayOfWeek);
        if (requestedStart != null) payload.put("requestedStart", requestedStart);
        if (requestedEnd != null) payload.put("requestedEnd", requestedEnd);
        if (reason != null) payload.put("reason", reason);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                baseRequest("").POST(
                        HttpRequest.BodyPublishers.ofString(payload.toString())
                ).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    // Админ — все запросы
    public static JsonNode getAllRequests() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                baseRequest("").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    // Админ — одобрить/отклонить
    public static JsonNode reviewRequest(Long id, String status,
                                         String adminComment) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("status", status);
        if (adminComment != null) payload.put("adminComment", adminComment);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                baseRequest("/" + id).method("PATCH",
                        HttpRequest.BodyPublishers.ofString(payload.toString())
                ).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }
}