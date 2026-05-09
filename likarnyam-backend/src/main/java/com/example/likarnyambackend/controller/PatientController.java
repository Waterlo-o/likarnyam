package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.model.Patient;
import com.example.likarnyambackend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // GET http://localhost:8080/api/patients
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    // GET http://localhost:8080/api/patients/1
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET http://localhost:8080/api/patients/search?lastName=Митчелл
    @GetMapping("/search")
    public List<Patient> search(@RequestParam String lastName) {
        return patientService.searchByLastName(lastName);
    }

    // POST http://localhost:8080/api/patients
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }
}