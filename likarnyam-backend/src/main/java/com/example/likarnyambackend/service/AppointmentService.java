package com.example.likarnyambackend.service;

import com.example.likarnyambackend.dto.request.AppointmentCreateRequest;
import com.example.likarnyambackend.dto.response.AppointmentResponse;
import com.example.likarnyambackend.model.Appointment;
import com.example.likarnyambackend.model.Doctor;
import com.example.likarnyambackend.repository.AppointmentRepository;
import com.example.likarnyambackend.repository.PatientRepository;
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

    private final PatientRepository patientRepository;

    public AppointmentResponse createAppointment(
            AppointmentCreateRequest request, Doctor doctor) {

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")));
        appointment.setAppointmentAt(request.getAppointmentAt());
        appointment.setDurationMinutes(30);
        appointment.setStatus("SCHEDULED");
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedAt(java.time.LocalDateTime.now());

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }


    public List<Appointment> getDoctorAppointments(Long doctorId, String status) {
        if (status != null && !status.isEmpty()) {
            return appointmentRepository
                    .findByDoctorIdAndStatus(doctorId, status);
        }
        return appointmentRepository
                .findByDoctorIdOrderByAppointmentAtDesc(doctorId);
    }
}