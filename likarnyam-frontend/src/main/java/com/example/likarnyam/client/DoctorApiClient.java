package com.example.likarnyam.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.likarnyam.session.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DoctorApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getMe() throws Exception {
        String token = UserSession.getInstance().getJwtToken();
        System.out.println("Token in getMe: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
        System.out.println("Connecting to: " + BASE_URL + "/doctors/me");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/doctors/me"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("DoctorApiClient status: " + response.statusCode());

            if (response.statusCode() == 403 || response.statusCode() == 401) {
                throw new RuntimeException("TOKEN_EXPIRED");
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed: " + response.statusCode());
            }

            return objectMapper.readTree(response.body());

        } catch (Exception e) {
            System.out.println("getMe exception: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }

    public static JsonNode updateProfile(String firstName, String lastName,
                                         String phone) throws Exception {
        System.out.println("Updating profile: " + firstName + " " + lastName);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"phone\":\"%s\"}",
                firstName, lastName, phone
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/doctors/me"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " +
                        UserSession.getInstance().getJwtToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Update response: " + response.statusCode());
        System.out.println("Update body: " + response.body());

        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());

        return objectMapper.readTree(response.body());
    }
}