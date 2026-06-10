package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.DoctorDayOff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorDayOffRepository extends JpaRepository<DoctorDayOff, Long> {
    Optional<DoctorDayOff> findByDoctorIdAndDate(Long doctorId, LocalDate date);
    List<DoctorDayOff> findByDoctorIdAndDateBetween(Long doctorId, LocalDate from, LocalDate to);
    boolean existsByDoctorIdAndDate(Long doctorId, LocalDate date);
}