package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class EventApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Единый клиент с таймаутом для оптимизации ресурсов
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Общий метод проверки статуса (перехват 401/403)
    private static void validateResponse(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new AuthExpiredException("Token is expired or invalid.");
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("API Request failed with status: " + status);
        }
    }

    // GET /api/events/upcoming
    public static JsonNode getUpcoming() throws Exception {
        return sendGetRequest("/events/upcoming");
    }

    // GET /api/events (все события)
    public static JsonNode getAllEvents() throws Exception {
        return sendGetRequest("/events");
    }

    // POST /api/events (создать новое)
    public static JsonNode createEvent(String title, String description, String eventAt, String location, String eventType) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("eventAt", eventAt); // Ожидается строка в ISO формате, например: "2026-05-27T10:00:00"
        body.put("location", location);
        body.put("eventType", eventType);

        String jsonBody = objectMapper.writeValueAsString(body);
        return sendRequestWithBody("/events", "POST", jsonBody);
    }

    // PATCH /api/events/{id} (редактировать)
    public static JsonNode updateEvent(Long eventId, String title, String description, String eventAt, String location, String eventType) throws Exception {
        Map<String, String> body = new HashMap<>();
        // Добавляем только те поля, которые реально переданы для обновления
        if (title != null) body.put("title", title);
        if (description != null) body.put("description", description);
        if (eventAt != null) body.put("eventAt", eventAt);
        if (location != null) body.put("location", location);
        if (eventType != null) body.put("eventType", eventType);

        String jsonBody = objectMapper.writeValueAsString(body);
        return sendRequestWithBody("/events/" + eventId, "PATCH", jsonBody);
    }

    // DELETE /api/events/{id} (удалить)
    public static void deleteEvent(Long eventId) throws Exception {
        String token = UserSession.getInstance().getJwtToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/" + eventId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
    }

    // --- Вспомогательные приватные методы для сокращения дублирования ---

    private static JsonNode sendGetRequest(String endpoint) throws Exception {
        String token = UserSession.getInstance().getJwtToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);

        return objectMapper.readTree(response.body());
    }

    private static JsonNode sendRequestWithBody(String endpoint, String method, String jsonBody) throws Exception {
        String token = UserSession.getInstance().getJwtToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method(method, HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);

        return objectMapper.readTree(response.body());
    }
}