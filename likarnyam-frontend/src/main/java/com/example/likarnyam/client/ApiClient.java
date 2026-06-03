package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static com.fasterxml.jackson.databind.ObjectMapper getMapper() {
        return objectMapper;
    }

    public static String login(String email, String password) throws Exception {

        String requestBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password
        );

        System.out.println("Sending login request for: " + email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed: " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("token").asText();
    }
    public static void changePassword(String currentPassword,
                                      String newPassword) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String body = String.format(
                "{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}",
                currentPassword, newPassword
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " +
                        UserSession.getInstance().getJwtToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request, HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Invalid current password");
    }

    // Универсальный метод для POST-запросов
    public static JsonNode post(String endpoint, JsonNode payload) throws Exception {
        String token = UserSession.getInstance().getJwtToken();
        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 400) {
            throw new RuntimeException("POST request failed with status: " + response.statusCode() + ", body: " + response.body());
        }

        // Если сервер возвращает пустой ответ (например 204 No Content), возвращаем пустой объект
        if (response.body() == null || response.body().isBlank()) {
            return objectMapper.createObjectNode();
        }

        return objectMapper.readTree(response.body());
    }

    public static JsonNode put(String endpoint, JsonNode payload) throws Exception {
        String token = com.example.likarnyam.session.UserSession.getInstance().getJwtToken();
        String jsonBody = objectMapper.writeValueAsString(payload);

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody)) // Используем PUT
                .build();

        java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("PUT request failed: " + response.statusCode());
        }
        if (response.body() == null || response.body().isBlank()) return objectMapper.createObjectNode();
        return objectMapper.readTree(response.body());
    }

}