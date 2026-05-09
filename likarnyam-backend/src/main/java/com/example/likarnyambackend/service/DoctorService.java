package com.example.likarnyambackend.service;

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
}