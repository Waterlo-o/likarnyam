package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Event;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventAt;
    private String location;
    private String eventType;

    public static EventResponse from(Event event) {
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventAt(event.getEventAt());
        dto.setLocation(event.getLocation());
        dto.setEventType(event.getEventType());
        return dto;
    }
}