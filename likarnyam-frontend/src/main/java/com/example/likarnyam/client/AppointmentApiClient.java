package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class AppointmentApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    // Используем один клиент для всех запросов — это быстрее и экономит память
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Общий метод проверки статуса, как в DoctorApiClient
    private static void validateResponse(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new AuthExpiredException("Token is expired or invalid.");
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("API Request failed with status: " + status);
        }
    }

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static JsonNode getTodayAppointments() throws Exception {
        HttpRequest request = baseRequest("/appointments/today").GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    public static void createAppointment(Long patientId, LocalDateTime appointmentAt,
                                         String reason, String notes) throws Exception {
        String body = String.format(
                "{\"patientId\":%d,\"appointmentAt\":\"%s\",\"reason\":\"%s\",\"notes\":\"%s\"}",
                patientId, appointmentAt.toString(), reason, notes
        );
        HttpRequest request = baseRequest("/appointments")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
    }

    public static void updateStatus(Long appointmentId, String status) throws Exception {
        System.out.println("Updating status: id=" + appointmentId + " status=" + status);
        HttpRequest request = baseRequest("/appointments/" + appointmentId + "/status?status=" + status)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Update status response: " + response.statusCode());
        System.out.println("Update status body: " + response.body());

        validateResponse(response);
    }

    public static JsonNode getAllAppointments(String status) throws Exception {
        String path = "/appointments";
        if (status != null && !status.isEmpty()) {
            path += "?status=" + status;
        }
        HttpRequest request = baseRequest(path).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
        return objectMapper.readTree(response.body());
    }

    public static void updateAppointment(Long appointmentId, LocalDateTime appointmentAt,
                                         String reason, String notes) throws Exception {
        String body = String.format(
                "{\"appointmentAt\":\"%s\",\"reason\":\"%s\",\"notes\":\"%s\"}",
                appointmentAt.toString(), reason, notes
        );

        HttpRequest request = baseRequest("/appointments/" + appointmentId)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
    }

    public static void deleteAppointment(Long appointmentId) throws Exception {
        HttpRequest request = baseRequest("/appointments/" + appointmentId).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }
}