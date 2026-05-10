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
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Получить профиль залогиненного врача
    public static JsonNode getMe() throws Exception {

        // Берём токен из сессии
        String token = UserSession.getInstance().getJwtToken();

        // Строим GET запрос с токеном в заголовке
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/doctors/me"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get doctor profile: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }
}