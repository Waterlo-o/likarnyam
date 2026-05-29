package com.example.likarnyambackend.dto.request;

import java.time.LocalDateTime;

public record EventRequest(
        String title,
        String description,
        LocalDateTime eventAt,
        String location,
        String eventType
) {}