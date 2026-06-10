package com.example.likarnyambackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "schedule_change_requests")
public class ScheduleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private Integer dayOfWeek;

    private LocalTime requestedStart;
    private LocalTime requestedEnd;
    private String reason;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String adminComment;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Doctor reviewedBy;

    @Column(nullable = false)
    private String requestType = "CHANGE"; // CHANGE или DAY_OFF


    private java.time.LocalDate requestedDate; // для DAY_OFF

    @Column(name = "hidden_by_doctor", nullable = false)
    private boolean hiddenByDoctor = false;

}
