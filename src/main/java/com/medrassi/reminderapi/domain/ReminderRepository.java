package com.medrassi.reminderapi.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage seam. The in-memory implementation is enough to get the delivery
 * pipeline green; swapping in Spring Data JPA later touches only this package.
 */
public interface ReminderRepository {

    List<Reminder> findAll();

    Optional<Reminder> findById(UUID id);

    Reminder save(Reminder reminder);

    boolean deleteById(UUID id);
}
