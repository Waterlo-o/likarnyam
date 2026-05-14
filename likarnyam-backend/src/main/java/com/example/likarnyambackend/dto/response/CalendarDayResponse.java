package com.example.likarnyambackend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CalendarDayResponse {
    private int day;
    private boolean isWorkingDay;
    private boolean isToday;
    private int appointmentCount;
    private List<String> appointmentTimes;
}