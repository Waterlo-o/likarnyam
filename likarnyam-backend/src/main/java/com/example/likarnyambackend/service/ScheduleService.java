package com.example.likarnyambackend.service;

import com.example.likarnyambackend.model.Appointment;
import com.example.likarnyambackend.model.Doctor;
import com.example.likarnyambackend.model.Schedule;
import com.example.likarnyambackend.repository.AppointmentRepository;
import com.example.likarnyambackend.repository.ScheduleRepository;
import com.example.likarnyambackend.dto.response.CalendarDayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    // Получить расписание врача
    public List<Schedule> getDoctorSchedule(Long doctorId) {
        return scheduleRepository
                .findByDoctorIdAndIsActiveTrueOrderByDayOfWeek(doctorId);
    }

    // Сохранить расписание
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    // Получить свободные слоты на дату
    public List<LocalTime> getAvailableSlots(Doctor doctor, LocalDate date) {

        // Определяем день недели (1=Пн...7=Вс)
        int dayOfWeek = date.getDayOfWeek().getValue();

        // Ищем расписание на этот день
        List<Schedule> schedules = scheduleRepository
                .findByDoctorIdAndIsActiveTrueOrderByDayOfWeek(doctor.getId());

        Schedule daySchedule = schedules.stream()
                .filter(s -> s.getDayOfWeek().equals(dayOfWeek))
                .findFirst()
                .orElse(null);

        // Если врач не работает в этот день — нет слотов
        if (daySchedule == null) return List.of();

        // Получаем уже занятые приёмы на эту дату
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        List<Appointment> existing = appointmentRepository
                .findTodayByDoctorId(doctor.getId(), start, end);

        // Генерируем все слоты
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = daySchedule.getStartTime();

        while (current.isBefore(daySchedule.getEndTime())) {
            final LocalTime slot = current;

            // Проверяем не занят ли слот
            boolean taken = existing.stream().anyMatch(a ->
                    a.getAppointmentAt().toLocalTime().equals(slot)
            );

            if (!taken) slots.add(slot);
            current = current.plusMinutes(daySchedule.getSlotDurationMinutes());
        }

        return slots;
    }
    public List<CalendarDayResponse> getCalendarData(Doctor doctor, int year, int month) {
        List<Schedule> schedules = getDoctorSchedule(doctor.getId());
        List<Appointment> appointments = appointmentService.getMonthAppointments(
                doctor.getId(), year, month
        );

        LocalDate today = LocalDate.now();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        List<CalendarDayResponse> result = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            int dayOfWeek = date.getDayOfWeek().getValue();

            CalendarDayResponse dto = new CalendarDayResponse();
            dto.setDay(day);
            dto.setToday(date.equals(today));

            // Проверяем рабочий ли день
            boolean isWorking = schedules.stream()
                    .anyMatch(s -> s.getDayOfWeek().equals(dayOfWeek));
            dto.setWorkingDay(isWorking);

            // Считаем приёмы на этот день
            List<Appointment> dayAppts = appointments.stream()
                    .filter(a -> a.getAppointmentAt().toLocalDate().equals(date))
                    .toList();

            dto.setAppointmentCount(dayAppts.size());
            dto.setAppointmentTimes(dayAppts.stream()
                    .map(a -> a.getAppointmentAt().toLocalTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                    .toList());

            result.add(dto);
        }

        return result;
    }
}