package com.example.likarnyambackend.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatientCreateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodType;
    private List<Long> allergyIds;
}