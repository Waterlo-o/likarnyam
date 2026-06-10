package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.model.Doctor;
import com.example.likarnyambackend.model.DoctorDayOff;
import com.example.likarnyambackend.repository.AppointmentRepository;
import com.example.likarnyambackend.repository.DoctorDayOffRepository;
import com.example.likarnyambackend.model.ScheduleChangeRequest;
import com.example.likarnyambackend.repository.ScheduleChangeRequestRepository;
import com.example.likarnyambackend.repository.ScheduleRepository;
import com.example.likarnyambackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule/requests")
@RequiredArgsConstructor
public class ScheduleChangeRequestController {

    private final ScheduleChangeRequestRepository requestRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorService doctorService;
    private final com.example.likarnyambackend.repository.UserRepository userRepository;
    private final DoctorDayOffRepository dayOffRepository;
    private final AppointmentRepository appointmentRepository;

    // Врач — свои запросы
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyRequests(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        requestRepository.findByDoctorIdOrderByCreatedAtDesc(doctor.getId())
                                .stream()
                                .filter(req -> !req.isHiddenByDoctor())
                                .map(this::toMap).toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<Void> hideRequest(@PathVariable Long id, Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .flatMap(doctor -> requestRepository.findById(id).map(req -> {
                    // Проверяем, что врач скрывает именно свою заявку
                    if (req.getDoctor().getId().equals(doctor.getId())) {
                        req.setHiddenByDoctor(true);
                        requestRepository.save(req);
                        return ResponseEntity.ok().<Void>build();
                    }
                    return ResponseEntity.status(403).<Void>build();
                }))
                .orElse(ResponseEntity.notFound().build());
    }

    // Врач — создать запрос
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(
            @RequestBody Map<String, Object> body,
            Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> {
                    ScheduleChangeRequest req = new ScheduleChangeRequest();
                    req.setDoctor(doctor);

                    String requestType = (String) body.getOrDefault("requestType", "CHANGE");
                    req.setRequestType(requestType);

                    if ("DAY_OFF".equals(requestType)) {
                        // Для отгула dayOfWeek не нужен
                        req.setDayOfWeek(null);
                    } else {
                        req.setDayOfWeek((Integer) body.get("dayOfWeek"));
                    }

                    if (body.get("requestedDate") != null) {
                        req.setRequestedDate(java.time.LocalDate.parse((String) body.get("requestedDate")));
                    }

                    req.setRequestedStart(body.get("requestedStart") != null
                            ? LocalTime.parse((String) body.get("requestedStart")) : null);
                    req.setRequestedEnd(body.get("requestedEnd") != null
                            ? LocalTime.parse((String) body.get("requestedEnd")) : null);
                    req.setReason((String) body.get("reason"));
                    req.setStatus("PENDING");
                    req.setCreatedAt(LocalDateTime.now());

                    return ResponseEntity.ok(toMap(requestRepository.save(req)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Админ — все запросы
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRequests(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .map(user -> {
                    if (!"ADMIN".equals(user.getRole().getName())) {
                        return ResponseEntity.status(403)
                                .<List<Map<String, Object>>>build();
                    }
                    return ResponseEntity.ok(
                            requestRepository.findAllByOrderByCreatedAtDesc()
                                    .stream().map(this::toMap).toList()
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Админ — одобрить/отклонить
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> reviewRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .flatMap(user -> {
                    if (!"ADMIN".equals(user.getRole().getName()))
                        return java.util.Optional.empty();
                    return requestRepository.findById(id).map(req -> {
                        String status = (String) body.get("status");
                        req.setStatus(status);
                        req.setAdminComment((String) body.get("adminComment"));
                        req.setReviewedAt(LocalDateTime.now());

                        if ("APPROVED".equals(status)) {
                            applyScheduleChange(req);
                        }

                        return toMap(requestRepository.save(req));
                    });
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    private void applyScheduleChange(ScheduleChangeRequest req) {
        if ("DAY_OFF".equals(req.getRequestType())) {
            if (req.getRequestedDate() != null) {
                LocalDate date = req.getRequestedDate();
                long existingAppointments = appointmentRepository
                        .findTodayByDoctorId(
                                req.getDoctor().getId(),
                                date.atStartOfDay(),
                                date.atTime(23, 59, 59)
                        ).stream()
                        .filter(a -> "SCHEDULED".equals(a.getStatus()))
                        .count();

                DoctorDayOff dayOff = new DoctorDayOff();
                dayOff.setDoctor(req.getDoctor());
                dayOff.setDate(date);
                dayOff.setReason(req.getReason());
                dayOffRepository.save(dayOff);
            }
            return;
        }

        scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByDayOfWeek(
                        req.getDoctor().getId()
                ).stream()
                .filter(s -> s.getDayOfWeek().equals(req.getDayOfWeek()))
                .findFirst()
                .ifPresent(schedule -> {
                    if (req.getRequestedStart() != null)
                        schedule.setStartTime(req.getRequestedStart());
                    if (req.getRequestedEnd() != null)
                        schedule.setEndTime(req.getRequestedEnd());
                    scheduleRepository.save(schedule);
                });
    }


    private Map<String, Object> toMap(ScheduleChangeRequest req) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", req.getId());
        map.put("doctorName", req.getDoctor().getFirstName() + " " +
                req.getDoctor().getLastName());
        map.put("doctorId", req.getDoctor().getId());
        map.put("dayOfWeek", req.getDayOfWeek());
        map.put("dayName", req.getDayOfWeek() != null ? getDayName(req.getDayOfWeek()) : null);
        map.put("requestedStart", req.getRequestedStart() != null
                ? req.getRequestedStart().toString() : null);
        map.put("requestedEnd", req.getRequestedEnd() != null
                ? req.getRequestedEnd().toString() : null);
        map.put("reason", req.getReason());
        map.put("status", req.getStatus());
        map.put("adminComment", req.getAdminComment());
        map.put("createdAt", req.getCreatedAt() != null
                ? req.getCreatedAt().toString() : null);
        map.put("requestType", req.getRequestType());
        if ("DAY_OFF".equals(req.getRequestType()) && req.getRequestedDate() != null) {
            LocalDate date = req.getRequestedDate();
            long count = appointmentRepository.findTodayByDoctorId(
                            req.getDoctor().getId(),
                            date.atStartOfDay(),
                            date.atTime(23, 59, 59)
                    ).stream()
                    .filter(a -> "SCHEDULED".equals(a.getStatus()))
                    .count();
            map.put("existingAppointments", count);
        }
        return map;
    }

    private String getDayName(Integer dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Unknown";
        };
    }
}