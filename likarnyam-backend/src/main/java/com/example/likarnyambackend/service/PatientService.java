package com.example.likarnyambackend.service;

import com.example.likarnyambackend.dto.request.PatientUpdateRequest;
import com.example.likarnyambackend.model.Allergy;
import com.example.likarnyambackend.model.Patient;
import com.example.likarnyambackend.repository.AllergyRepository;
import com.example.likarnyambackend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.example.likarnyambackend.dto.request.PatientCreateRequest;
import com.example.likarnyambackend.model.Allergy;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final AllergyRepository allergyRepository;

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

    public Patient createPatient(PatientCreateRequest request) {
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());

        if (request.getAllergyIds() != null && !request.getAllergyIds().isEmpty()) {
            Set<Allergy> allergies = new HashSet<>(
                    allergyRepository.findAllById(request.getAllergyIds())
            );
            patient.setAllergies(allergies);
        }

        return patientRepository.save(patient);
    }

    public Optional<Patient> updatePatient(Long id, PatientUpdateRequest updated) {
        return patientRepository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setPhone(updated.getPhone());
            existing.setEmail(updated.getEmail());
            existing.setDateOfBirth(updated.getDateOfBirth());
            existing.setGender(updated.getGender());
            existing.setBloodType(updated.getBloodType());
            existing.setUpdatedAt(LocalDateTime.now());

            if (updated.getAllergyIds() != null) {
                Set<Allergy> allergies = new HashSet<>(
                        allergyRepository.findAllById(updated.getAllergyIds())
                );
                existing.setAllergies(allergies);
            }

            return patientRepository.save(existing);
        });
    }


}