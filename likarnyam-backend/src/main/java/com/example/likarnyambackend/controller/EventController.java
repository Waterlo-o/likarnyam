package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.request.EventRequest;
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

    // GET /api/events/upcoming (оставляем как было)
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcoming(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        eventRepository.findUpcomingByDoctorId(doctor.getId(), LocalDateTime.now())
                                .stream()
                                .map(EventResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/events -> Получить все события врача (для экрана View All)
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> ResponseEntity.ok(
                        eventRepository.findByDoctorIdOrderByEventAtDesc(doctor.getId())
                                .stream()
                                .map(EventResponse::from)
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/events -> Создать новое событие
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request, Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .map(doctor -> {
                    Event event = new Event();
                    event.setDoctor(doctor);
                    event.setTitle(request.title());
                    event.setDescription(request.description());
                    event.setEventAt(request.eventAt());
                    event.setLocation(request.location());
                    event.setEventType(request.eventType());

                    Event savedEvent = eventRepository.save(event);
                    return ResponseEntity.ok(EventResponse.from(savedEvent));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH /api/events/{id} -> Редактировать событие
    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request,
            Principal principal) {

        return doctorService.getDoctorByEmail(principal.getName())
                .flatMap(doctor -> eventRepository.findByIdAndDoctorId(id, doctor.getId()))
                .map(event -> {
                    if (request.title() != null) event.setTitle(request.title());
                    if (request.description() != null) event.setDescription(request.description());
                    if (request.eventAt() != null) event.setEventAt(request.eventAt());
                    if (request.location() != null) event.setLocation(request.location());
                    if (request.eventType() != null) event.setEventType(request.eventType());

                    return ResponseEntity.ok(EventResponse.from(eventRepository.save(event)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/events/{id} -> Удалить событие
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName())
                .flatMap(doctor -> eventRepository.findByIdAndDoctorId(id, doctor.getId()))
                .map(event -> {
                    eventRepository.delete(event);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}