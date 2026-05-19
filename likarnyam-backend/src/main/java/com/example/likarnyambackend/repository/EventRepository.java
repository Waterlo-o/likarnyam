package com.example.likarnyambackend.repository;

import com.example.likarnyambackend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.doctor.id = :doctorId " +
            "AND e.eventAt >= :now " +
            "ORDER BY e.eventAt ASC")
    List<Event> findUpcomingByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("now") LocalDateTime now
    );
}