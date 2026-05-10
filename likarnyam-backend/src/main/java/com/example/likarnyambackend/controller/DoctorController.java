package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.response.DoctorResponse;
import com.example.likarnyambackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(DoctorResponse.from(doctor)))
                .orElse(ResponseEntity.notFound().<DoctorResponse>build());
    }

    @GetMapping("/me")
    public ResponseEntity<DoctorResponse> getMe(Principal principal) {
        String email = principal.getName();
        return doctorService.getDoctorByEmail(email)
                .map(doctor -> ResponseEntity.ok(DoctorResponse.from(doctor)))
                .orElse(ResponseEntity.notFound().<DoctorResponse>build());
    }
}