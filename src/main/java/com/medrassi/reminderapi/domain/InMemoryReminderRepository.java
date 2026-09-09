package com.medrassi.reminderapi.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryReminderRepository implements ReminderRepository {

    private final Map<UUID, Reminder> store = new ConcurrentHashMap<>();

    public InMemoryReminderRepository() {
        seed("Renew car insurance", "Policy expires at midnight.", LocalDate.now().plusDays(2), LocalTime.of(9, 0));
        seed("Team retrospective", "Bring the deployment metrics.", LocalDate.now().plusDays(4), LocalTime.of(14, 30));
        seed("Mum's birthday", null, LocalDate.now().plusDays(11), null);
    }

    private void seed(String title, String notes, LocalDate date, LocalTime time) {
        UUID id = UUID.randomUUID();
        store.put(id, new Reminder(id, title, notes, date, time, false, Instant.now()));
    }

    @Override
    public List<Reminder> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Reminder::dueDate)
                        .thenComparing(r -> r.dueTime() == null ? LocalTime.MIN : r.dueTime())
                        .thenComparing(Reminder::title))
                .toList();
    }

    @Override
    public Optional<Reminder> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Reminder save(Reminder reminder) {
        store.put(reminder.id(), reminder);
        return reminder;
    }

    @Override
    public boolean deleteById(UUID id) {
        return store.remove(id) != null;
    }
}
