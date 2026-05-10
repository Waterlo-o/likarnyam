package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Appointment;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {

    private Long id;
    private String patientFirstName;
    private String patientLastName;
    private String reason;
    private String status;
    private LocalDateTime appointmentAt;
    private Integer durationMinutes;

    public static AppointmentResponse from(Appointment appointment) {
        AppointmentResponse dto = new AppointmentResponse();
        dto.setId(appointment.getId());
        dto.setPatientFirstName(appointment.getPatient().getFirstName());
        dto.setPatientLastName(appointment.getPatient().getLastName());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus());
        dto.setAppointmentAt(appointment.getAppointmentAt());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        return dto;
    }
}