package com.example.likarnyambackend.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentCreateRequest {
    private Long patientId;
    private LocalDateTime appointmentAt;
    private String reason;
    private String notes;
}