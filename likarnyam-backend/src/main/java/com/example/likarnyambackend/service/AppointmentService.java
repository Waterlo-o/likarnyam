package com.example.likarnyambackend.service;

import com.example.likarnyambackend.model.Appointment;
import com.example.likarnyambackend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public List<Appointment> getTodayAppointments(Long doctorId) {

        LocalDateTime start = LocalDate.now().atStartOfDay();

         LocalDateTime end = LocalDate.now().atTime(23, 59, 59);

        return appointmentRepository.findTodayByDoctorId(doctorId, start, end);
    }
}