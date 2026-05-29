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
            throw new AuthExpiredException("Token is expired or invalid.");
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
}