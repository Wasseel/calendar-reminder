package com.medrassi.reminderapi.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medrassi.reminderapi.domain.Reminder;
import com.medrassi.reminderapi.domain.ReminderNotFoundException;
import com.medrassi.reminderapi.domain.ReminderRepository;
import com.medrassi.reminderapi.web.dto.CalendarDay;
import com.medrassi.reminderapi.web.dto.CalendarMonth;
import com.medrassi.reminderapi.web.dto.ReminderRequest;

@Service
public class ReminderService {

    private final ReminderRepository repository;

    public ReminderService(ReminderRepository repository) {
        this.repository = repository;
    }

    public List<Reminder> list(Boolean done, LocalDate from, LocalDate to) {
        return repository.findAll().stream()
                .filter(r -> done == null || r.done() == done)
                .filter(r -> from == null || !r.dueDate().isBefore(from))
                .filter(r -> to == null || !r.dueDate().isAfter(to))
                .toList();
    }

    public Reminder get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ReminderNotFoundException(id));
    }

    public Reminder create(ReminderRequest request) {
        Reminder reminder = new Reminder(
                UUID.randomUUID(),
                request.title().trim(),
                request.notes(),
                request.dueDate(),
                request.dueTime(),
                false,
                Instant.now());
        return repository.save(reminder);
    }

    public Reminder replace(UUID id, ReminderRequest request) {
        Reminder existing = get(id);
        Reminder updated = new Reminder(
                existing.id(),
                request.title().trim(),
                request.notes(),
                request.dueDate(),
                request.dueTime(),
                existing.done(),
                existing.createdAt());
        return repository.save(updated);
    }

    public Reminder setDone(UUID id, boolean done) {
        Reminder existing = get(id);
        return repository.save(done ? existing.completed() : existing.reopened());
    }

    public void delete(UUID id) {
        if (!repository.deleteById(id)) {
            throw new ReminderNotFoundException(id);
        }
    }

    /** Everything still open between today and {@code days} ahead, inclusive. */
    public List<Reminder> upcoming(int days) {
        LocalDate today = LocalDate.now();
        return list(false, today, today.plusDays(days));
    }

    public CalendarMonth month(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<Reminder> inMonth = list(null, ym.atDay(1), ym.atEndOfMonth());

        Map<LocalDate, List<Reminder>> byDay = inMonth.stream()
                .collect(Collectors.groupingBy(Reminder::dueDate, LinkedHashMap::new, Collectors.toList()));

        List<CalendarDay> days = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new CalendarDay(e.getKey(), e.getValue().size(), e.getValue()))
                .sorted(Comparator.comparing(CalendarDay::date))
                .toList();

        return new CalendarMonth(year, month, inMonth.size(), days);
    }
}
