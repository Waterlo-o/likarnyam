package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SymptomApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getAll() throws Exception {
        String token = UserSession.getInstance().getJwtToken();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/symptoms"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }
}