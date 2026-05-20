package com.example.likarnyambackend.service;

import com.example.likarnyambackend.dto.request.UpdateDoctorRequest;
import com.example.likarnyambackend.model.Doctor;
import com.example.likarnyambackend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    // Найти врача по email пользователя
    // Используется для endpoint /api/doctors/me
    public Optional<Doctor> getDoctorByEmail(String email) {
        return doctorRepository.findByUserEmail(email);
    }

    public Doctor updateDoctor(Doctor doctor, UpdateDoctorRequest request) {
        if (request.getFirstName() != null)
            doctor.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            doctor.setLastName(request.getLastName());
        if (request.getPhone() != null)
            doctor.setPhone(request.getPhone());
        return doctorRepository.save(doctor);
    }
}