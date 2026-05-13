package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.response.AppointmentResponse;
import com.example.likarnyambackend.service.AppointmentService;
import com.example.likarnyambackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    @GetMapping("/today")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        appointmentService.getTodayAppointments(doctor.getId())
                                .stream()
                                .map(AppointmentResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }
    // GET /api/appointments/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getPatientHistory(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                appointmentService.getPatientHistory(patientId)
                        .stream()
                        .map(AppointmentResponse::from)
                        .toList()
        );
    }
}