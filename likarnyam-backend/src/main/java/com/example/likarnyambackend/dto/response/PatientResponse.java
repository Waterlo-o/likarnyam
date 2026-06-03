package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Patient;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatientResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String bloodType;
    private List<AllergyDto> allergies;

    @Data
    public static class AllergyDto {
        private Long id;
        private String name;
        private String icon;
    }
    private String notes;

    public static PatientResponse from(Patient patient) {
        PatientResponse dto = new PatientResponse();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setMiddleName(patient.getMiddleName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setPhone(patient.getPhone());
        dto.setEmail(patient.getEmail());
        dto.setBloodType(patient.getBloodType());
        dto.setAllergies(patient.getAllergies().stream()
                .map(a -> {
                    PatientResponse.AllergyDto dto2 = new PatientResponse.AllergyDto();
                    dto2.setId(a.getId());
                    dto2.setName(a.getName());
                    dto2.setIcon(a.getIcon());
                    return dto2;
                }).toList());
        dto.setNotes(patient.getNotes());
        return dto;
    }
}