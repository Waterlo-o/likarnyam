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
    private List<AppointmentInfo> appointments;

    @Data
    public static class AppointmentInfo {
        private String time;
        private String patientName;
        private String reason;
        private Long appointmentId;
        private String status;
        private List<SymptomInfo> symptoms;

        @Data
        public static class SymptomInfo {
            private Long id;
            private String name;
            private String icon;
            private String category;
        }
    }
}