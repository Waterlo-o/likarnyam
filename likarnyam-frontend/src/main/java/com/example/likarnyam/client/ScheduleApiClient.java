package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

public class ScheduleApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Единый клиент с таймаутом
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Общий метод проверки статуса
    private static void validateResponse(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new AuthExpiredException("Token is expired or invalid.");
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("API Request failed with status: " + status);
        }
    }

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static JsonNode getCalendar(int year, int month) throws Exception {
        HttpRequest request = baseRequest("/schedule/calendar?year=" + year + "&month=" + month)
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    public static JsonNode getAvailableSlots(LocalDate date) throws Exception {
        HttpRequest request = baseRequest("/schedule/slots?date=" + date.toString())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    public static JsonNode getMySchedule() throws Exception {
        HttpRequest request = baseRequest("/schedule/me").GET().build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return objectMapper.readTree(response.body());
    }
}