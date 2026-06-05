package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.ScheduleChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleChangeRequestRepository
        extends JpaRepository<ScheduleChangeRequest, Long> {

    List<ScheduleChangeRequest> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<ScheduleChangeRequest> findAllByOrderByCreatedAtDesc();
}