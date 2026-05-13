package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.model.Schedule;
import com.example.likarnyambackend.service.DoctorService;
import com.example.likarnyambackend.service.ScheduleService;
import com.example.likarnyambackend.dto.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final DoctorService doctorService;

    @GetMapping("/me")
    public ResponseEntity<List<ScheduleResponse>> getMySchedule(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        scheduleService.getDoctorSchedule(doctor.getId())
                                .stream()
                                .map(ScheduleResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> addSchedule(
            @RequestBody Schedule schedule,
            Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> {
                    schedule.setDoctor(doctor);
                    return ResponseEntity.ok(
                            ScheduleResponse.from(scheduleService.saveSchedule(schedule))
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/schedule/slots?date=2026-05-11 — свободные слоты
    @GetMapping("/slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        scheduleService.getAvailableSlots(doctor, date)
                ))
                .orElse(ResponseEntity.notFound().build());
    }


}