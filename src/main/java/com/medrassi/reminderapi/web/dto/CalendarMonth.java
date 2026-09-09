package com.medrassi.reminderapi.web.dto;

import java.util.List;

/** Sparse month view: only days that actually carry reminders are listed. */
public record CalendarMonth(int year, int month, int totalReminders, List<CalendarDay> days) {
}
