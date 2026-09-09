package com.medrassi.reminderapi.web.dto;

import java.time.LocalDate;
import java.util.List;

import com.medrassi.reminderapi.domain.Reminder;

public record CalendarDay(LocalDate date, int count, List<Reminder> reminders) {
}
