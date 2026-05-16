package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ScheduleApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static JsonNode getCalendar(int year, int month) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/schedule/calendar?year=" + year + "&month=" + month)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }
}