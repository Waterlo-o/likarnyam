package com.example.likarnyambackend.dto.response;

import com.example.likarnyambackend.model.Schedule;
import lombok.Data;
import java.time.LocalTime;

@Data
public class ScheduleResponse {

    private Long id;
    private Integer dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean isActive;

    public static ScheduleResponse from(Schedule schedule) {
        ScheduleResponse dto = new ScheduleResponse();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setDayName(getDayName(schedule.getDayOfWeek()));
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setSlotDurationMinutes(schedule.getSlotDurationMinutes());
        dto.setIsActive(schedule.getIsActive());
        return dto;
    }

    private static String getDayName(Integer day) {
        return switch (day) {
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