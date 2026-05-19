package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.response.EventResponse;
import com.example.likarnyambackend.model.Event;
import com.example.likarnyambackend.repository.EventRepository;
import com.example.likarnyambackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final DoctorService doctorService;

    // GET /api/events/upcoming
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcoming(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        eventRepository.findUpcomingByDoctorId(
                                        doctor.getId(), LocalDateTime.now()
                                )
                                .stream()
                                .map(EventResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }
}