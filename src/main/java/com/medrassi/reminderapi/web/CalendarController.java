package com.medrassi.reminderapi.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medrassi.reminderapi.service.ReminderService;
import com.medrassi.reminderapi.web.dto.CalendarMonth;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final ReminderService service;

    public CalendarController(ReminderService service) {
        this.service = service;
    }

    /** Month view, e.g. GET /api/calendar/2026/9 */
    @GetMapping("/{year}/{month}")
    public CalendarMonth month(@PathVariable int year, @PathVariable int month) {
        return service.month(year, month);
    }
}
