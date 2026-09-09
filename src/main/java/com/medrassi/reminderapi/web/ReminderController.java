package com.medrassi.reminderapi.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medrassi.reminderapi.domain.Reminder;
import com.medrassi.reminderapi.service.ReminderService;
import com.medrassi.reminderapi.web.dto.ReminderRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reminder> list(
            @RequestParam(required = false) Boolean done,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(done, from, to);
    }

    @GetMapping("/upcoming")
    public List<Reminder> upcoming(@RequestParam(defaultValue = "7") int days) {
        return service.upcoming(days);
    }

    @GetMapping("/{id}")
    public Reminder get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Reminder> create(@Valid @RequestBody ReminderRequest request) {
        Reminder created = service.create(request);
        return ResponseEntity.created(URI.create("/api/reminders/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Reminder replace(@PathVariable UUID id, @Valid @RequestBody ReminderRequest request) {
        return service.replace(id, request);
    }

    @PutMapping("/{id}/done")
    public Reminder complete(@PathVariable UUID id) {
        return service.setDone(id, true);
    }

    @DeleteMapping("/{id}/done")
    public Reminder reopen(@PathVariable UUID id) {
        return service.setDone(id, false);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
