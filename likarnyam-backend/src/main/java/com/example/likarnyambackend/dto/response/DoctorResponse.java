package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Doctor;
import lombok.Data;

@Data
public class DoctorResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String photoUrl;
    private String licenseNumber;
    private String specialization;
    private String email;
    private String theme;
    private String timeFormat;
    private boolean animationsEnabled;

    public static DoctorResponse from(Doctor doctor) {
        DoctorResponse dto = new DoctorResponse();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setMiddleName(doctor.getMiddleName());
        dto.setPhone(doctor.getPhone());
        dto.setPhotoUrl(doctor.getPhotoUrl());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setEmail(doctor.getUser().getEmail());

        if (doctor.getSpecialization() != null) {
            dto.setSpecialization(doctor.getSpecialization().getName());
        }

        dto.setTheme(doctor.getTheme() != null ? doctor.getTheme() : "LIGHT");
        dto.setTimeFormat(doctor.getTimeFormat() != null ? doctor.getTimeFormat() : "24h");
        dto.setAnimationsEnabled(doctor.isAnimationsEnabled());

        return dto;
    }
}