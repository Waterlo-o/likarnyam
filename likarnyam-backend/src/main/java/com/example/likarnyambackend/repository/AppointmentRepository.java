package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

     @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentAt BETWEEN :start AND :end " +
            "ORDER BY a.appointmentAt ASC")
    List<Appointment> findTodayByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    List<Appointment> findByPatientIdOrderByAppointmentAtDesc(Long patientId);
}