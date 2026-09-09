package com.medrassi.reminderapi.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A single reminder. {@code dueTime} is null for an all-day entry, which is why
 * the calendar groups on {@code dueDate} alone.
 */
public record Reminder(
        UUID id,
        String title,
        String notes,
        LocalDate dueDate,
        LocalTime dueTime,
        boolean done,
        Instant createdAt) {

    public Reminder completed() {
        return new Reminder(id, title, notes, dueDate, dueTime, true, createdAt);
    }

    public Reminder reopened() {
        return new Reminder(id, title, notes, dueDate, dueTime, false, createdAt);
    }
}
