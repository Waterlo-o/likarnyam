package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EventApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getUpcoming() throws Exception {
        String token = UserSession.getInstance().getJwtToken();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/events/upcoming"))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }
}