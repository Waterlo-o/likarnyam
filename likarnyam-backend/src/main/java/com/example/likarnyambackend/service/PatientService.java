package com.example.likarnyambackend.service;

import com.example.likarnyambackend.model.Patient;
import com.example.likarnyambackend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public List<Patient> searchByLastName(String lastName) {
        return patientRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    public Patient createPatient(Patient patient) {
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());
        return patientRepository.save(patient);
    }

    public Optional<Patient> updatePatient(Long id, Patient updated) {
        return patientRepository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setPhone(updated.getPhone());
            existing.setUpdatedAt(LocalDateTime.now());
            return patientRepository.save(existing);
        });
    }
}