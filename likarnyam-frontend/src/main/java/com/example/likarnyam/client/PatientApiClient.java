package com.example.likarnyam.client;

import com.example.likarnyam.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PatientApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static HttpRequest.Builder baseRequest(String path) {
        String token = UserSession.getInstance().getJwtToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

    public static JsonNode getAllPatients() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/patients").GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    public static JsonNode searchByLastName(String lastName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/patients/search?lastName=" + lastName).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    public static JsonNode getById(Long id) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/patients/" + id).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    public static JsonNode getPatientHistory(Long patientId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                baseRequest("/appointments/patient/" + patientId).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed: " + response.statusCode());
        return objectMapper.readTree(response.body());
    }

    public static JsonNode createPatient(String firstName, String lastName, String phone,
                                         String email, String birthDate, String gender,
                                         String bloodType, List<Long> allergyIds) throws Exception {
        com.fasterxml.jackson.databind.node.ObjectNode payload = ApiClient.getMapper().createObjectNode();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("phone", phone);
        if (email != null && !email.isEmpty()) payload.put("email", email);
        if (birthDate != null && !birthDate.isEmpty()) payload.put("dateOfBirth", birthDate);
        if (bloodType != null && !bloodType.isEmpty()) payload.put("bloodType", bloodType);
        if (gender != null && !gender.isEmpty()) payload.put("gender", gender);

        com.fasterxml.jackson.databind.node.ArrayNode allergiesArray =
                ApiClient.getMapper().createArrayNode();
        if (allergyIds != null) allergyIds.forEach(allergiesArray::add);
        payload.set("allergyIds", allergiesArray);

        return ApiClient.post("/patients", payload);
    }

    public static JsonNode updatePatient(Long patientId, String firstName, String lastName,
                                         String phone, String email, String birthDate,
                                         String gender, String bloodType,
                                         List<Long> allergyIds) throws Exception {
        com.fasterxml.jackson.databind.node.ObjectNode payload = ApiClient.getMapper().createObjectNode();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("phone", phone);
        if (email != null) payload.put("email", email);
        if (birthDate != null) payload.put("dateOfBirth", birthDate);
        if (gender != null) payload.put("gender", gender);
        if (bloodType != null) payload.put("bloodType", bloodType);

        com.fasterxml.jackson.databind.node.ArrayNode allergiesArray =
                ApiClient.getMapper().createArrayNode();
        if (allergyIds != null) {
            allergyIds.forEach(allergiesArray::add);
        }
        payload.set("allergyIds", allergiesArray);

        return ApiClient.put("/patients/" + patientId, payload);
    }


}