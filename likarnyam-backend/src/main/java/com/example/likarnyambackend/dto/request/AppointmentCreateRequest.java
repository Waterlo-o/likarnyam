package com.example.likarnyambackend.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppointmentCreateRequest {
    private Long patientId;
    private LocalDateTime appointmentAt;
    private String reason;
    private String notes;
    private List<Long> symptomIds;
}