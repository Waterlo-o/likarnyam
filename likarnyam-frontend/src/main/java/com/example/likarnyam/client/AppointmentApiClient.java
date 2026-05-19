package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class AppointmentApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getTodayAppointments() throws Exception {
        String token = UserSession.getInstance().getJwtToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/appointments/today"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    public static void createAppointment(Long patientId, LocalDateTime appointmentAt,
                                         String reason, String notes) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String body = String.format(
                "{\"patientId\":%d,\"appointmentAt\":\"%s\",\"reason\":\"%s\",\"notes\":\"%s\"}",
                patientId, appointmentAt.toString(), reason, notes
        );
        HttpResponse<String> response = client.send(
                baseRequest("/appointments")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
    }

    public static void updateStatus(Long appointmentId, String status) throws Exception {
        System.out.println("Updating status: id=" + appointmentId + " status=" + status);
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/appointments/" + appointmentId + "/status?status=" + status)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        System.out.println("Update status response: " + response.statusCode());
        System.out.println("Update status body: " + response.body());
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
    }

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api" + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static JsonNode getAllAppointments(String status) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String path = "/appointments";
        if (status != null && !status.isEmpty()) {
            path += "?status=" + status;
        }
        HttpResponse<String> response = client.send(
                baseRequest(path).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }
}