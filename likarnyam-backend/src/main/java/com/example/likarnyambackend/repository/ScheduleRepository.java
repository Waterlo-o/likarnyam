package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Расписание врача — все активные дни
    List<Schedule> findByDoctorIdAndIsActiveTrueOrderByDayOfWeek(Long doctorId);
}