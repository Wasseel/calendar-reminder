package com.medrassi.reminderapi.domain;

import java.util.UUID;

public class ReminderNotFoundException extends RuntimeException {

    public ReminderNotFoundException(UUID id) {
        super("No reminder with id " + id);
    }
}
