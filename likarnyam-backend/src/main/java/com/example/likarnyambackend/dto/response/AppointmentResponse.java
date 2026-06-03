package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Appointment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppointmentResponse {

    private Long id;
    private String patientFirstName;
    private String patientLastName;
    private String reason;
    private String status;
    private LocalDateTime appointmentAt;
    private Integer durationMinutes;
    private String notes;
    private List<SymptomDto> symptoms;
    private DiseaseDto disease;

    public static AppointmentResponse from(Appointment appointment) {
        AppointmentResponse dto = new AppointmentResponse();
        dto.setId(appointment.getId());
        dto.setPatientFirstName(appointment.getPatient().getFirstName());
        dto.setPatientLastName(appointment.getPatient().getLastName());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus());
        dto.setAppointmentAt(appointment.getAppointmentAt());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setNotes(appointment.getNotes() != null ? appointment.getNotes() : "No notes");

        dto.setSymptoms(appointment.getSymptoms().stream()
                .map(s -> {
                    SymptomDto symptomDto = new SymptomDto();
                    symptomDto.setId(s.getId());
                    symptomDto.setName(s.getName());
                    symptomDto.setIcon(s.getIcon());
                    symptomDto.setCategory(s.getCategory());
                    return symptomDto;
                }).toList());

        if (appointment.getDisease() != null) {
            DiseaseDto diseaseDto = new DiseaseDto();
            diseaseDto.setId(appointment.getDisease().getId());
            diseaseDto.setName(appointment.getDisease().getName());
            diseaseDto.setIcdCode(appointment.getDisease().getIcdCode());
            diseaseDto.setDescription(appointment.getDisease().getDescription());
            dto.setDisease(diseaseDto);
        }

        return dto;
    }

    @Data
    public static class SymptomDto {
        private Long id;
        private String name;
        private String icon;
        private String category;
    }

    @Data
    public static class DiseaseDto {
        private Long id;
        private String name;
        private String icdCode;
        private String description;
    }
}