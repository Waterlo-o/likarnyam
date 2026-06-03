package com.example.likarnyambackend.service;

import com.example.likarnyambackend.dto.request.AppointmentCreateRequest;
import com.example.likarnyambackend.dto.response.AppointmentResponse;
import com.example.likarnyambackend.model.Appointment;
import com.example.likarnyambackend.model.Doctor;
import com.example.likarnyambackend.model.Symptom;
import com.example.likarnyambackend.repository.AppointmentRepository;
import com.example.likarnyambackend.repository.DiseaseRepository;
import com.example.likarnyambackend.repository.PatientRepository;
import com.example.likarnyambackend.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentService {


    private final SymptomRepository symptomRepository;
    private final AppointmentRepository appointmentRepository;
    private final DiseaseRepository diseaseRepository;

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

        if (request.getSymptomIds() != null && !request.getSymptomIds().isEmpty()) {
            Set<Symptom> symptoms = new HashSet<>(
                    symptomRepository.findAllById(request.getSymptomIds())
            );
            appointment.setSymptoms(symptoms);
        }

        if (request.getDiseaseId() != null) {
            diseaseRepository.findById(request.getDiseaseId())
                    .ifPresent(appointment::setDisease);
        }

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


    public Optional<Appointment> updateAppointment(
            Long id,
            AppointmentCreateRequest request,
            Long doctorId) {

        return appointmentRepository.findById(id)
                .filter(apt -> apt.getDoctor().getId().equals(doctorId))
                .map(apt -> {
                    if (request.getAppointmentAt() != null)
                        apt.setAppointmentAt(request.getAppointmentAt());
                    if (request.getReason() != null)
                        apt.setReason(request.getReason());
                    if (request.getNotes() != null)
                        apt.setNotes(request.getNotes());

                    if (request.getSymptomIds() != null) {
                        Set<Symptom> symptoms = new HashSet<>(
                                symptomRepository.findAllById(request.getSymptomIds())
                        );
                        apt.setSymptoms(symptoms);
                    }

                    if (request.getDiseaseId() != null) {
                        diseaseRepository.findById(request.getDiseaseId())
                                .ifPresent(apt::setDisease);
                    } else {
                        apt.setDisease(null);
                    }

                    return appointmentRepository.save(apt);
                });
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}