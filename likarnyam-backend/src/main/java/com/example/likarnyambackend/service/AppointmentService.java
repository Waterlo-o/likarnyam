package com.example.likarnyambackend.service;

import com.example.likarnyambackend.model.Appointment;
import com.example.likarnyambackend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public List<Appointment> getTodayAppointments(Long doctorId) {

        LocalDateTime start = LocalDate.now().atStartOfDay();

         LocalDateTime end = LocalDate.now().atTime(23, 59, 59);

        return appointmentRepository.findTodayByDoctorId(doctorId, start, end);
    }
    public List<Appointment> getPatientHistory(Long patientId) {
        return appointmentRepository
                .findByPatientIdOrderByAppointmentAtDesc(patientId);
    }
    public List<Appointment> getMonthAppointments(Long doctorId, int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, month, 1)
                .plusMonths(1).atStartOfDay().minusSeconds(1);
        return appointmentRepository.findByDoctorIdAndMonth(doctorId, start, end);
    }
    public Optional<Appointment> updateStatus(Long id, String status) {
        return appointmentRepository.findById(id).map(apt -> {
            apt.setStatus(status);
            return appointmentRepository.save(apt);
        });
    }
}