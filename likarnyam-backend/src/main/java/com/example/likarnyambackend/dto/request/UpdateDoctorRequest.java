package com.example.likarnyambackend.dto.request;

import lombok.Data;

@Data
public class UpdateDoctorRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String theme;
    private String timeFormat;
    private Boolean animationsEnabled;
}