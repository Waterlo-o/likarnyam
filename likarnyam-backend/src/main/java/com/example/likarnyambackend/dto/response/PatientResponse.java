package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Patient;
import lombok.Data;
import java.time.LocalDate;

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
    private String allergies;
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
        dto.setAllergies(patient.getAllergies());
        dto.setNotes(patient.getNotes());
        return dto;
    }
}