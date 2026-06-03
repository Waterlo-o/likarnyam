package com.example.likarnyam.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.likarnyam.session.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DoctorApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static void validateResponse(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new RuntimeException("Token is expired or invalid."); // Заменил AuthExpiredException на RuntimeException, если у тебя нет кастомного класса
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("API Request failed with status: " + status);
        }
    }

    public static JsonNode getMe() throws Exception {
        String token = UserSession.getInstance().getJwtToken();
        System.out.println("Connecting to: " + BASE_URL + "/doctors/me");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/doctors/me"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    // Метод для вкладки Profile (Имя, Фамилия, Телефон)
    public static JsonNode updateProfile(String firstName, String lastName, String phone) throws Exception {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"phone\":\"%s\"}",
                firstName, lastName, phone
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/doctors/me"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + UserSession.getInstance().getJwtToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    // Метод для вкладки Appearance (Тема, Формат времени, Анимации)
    public static JsonNode updateAppearance(String theme, String timeFormat, boolean animationsEnabled) throws Exception {
        String body = String.format(
                "{\"theme\":\"%s\",\"timeFormat\":\"%s\",\"animationsEnabled\":%b}",
                theme, timeFormat, animationsEnabled
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/doctors/me"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + UserSession.getInstance().getJwtToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return objectMapper.readTree(response.body());
    }
}