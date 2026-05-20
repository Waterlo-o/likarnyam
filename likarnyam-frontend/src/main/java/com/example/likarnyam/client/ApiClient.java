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
}