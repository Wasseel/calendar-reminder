package com.medrassi.reminderapi.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for creating or replacing a reminder. Leave {@code dueTime} out for an all-day entry. */
public record ReminderRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be at most 120 characters")
        String title,

        @Size(max = 2000, message = "notes must be at most 2000 characters")
        String notes,

        @NotNull(message = "dueDate is required, as yyyy-MM-dd")
        LocalDate dueDate,

        LocalTime dueTime) {
}
