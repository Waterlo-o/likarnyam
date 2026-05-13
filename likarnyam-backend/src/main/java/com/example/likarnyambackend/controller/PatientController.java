package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.response.PatientResponse;
import com.example.likarnyambackend.model.Patient;
import com.example.likarnyambackend.service.DoctorService;
import com.example.likarnyambackend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getMyPatients(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        patientService.getAllPatients()
                                .stream()
                                .map(PatientResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> search(@RequestParam String lastName) {
        return ResponseEntity.ok(
                patientService.searchByLastName(lastName)
                        .stream()
                        .map(PatientResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(patient -> ResponseEntity.ok(PatientResponse.from(patient)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(
                PatientResponse.from(patientService.createPatient(patient))
        );
    }
}