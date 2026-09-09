package com.medrassi.reminderapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ReminderApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void healthEndpointReportsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readinessProbeIsExposedForKubernetes() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void listReturnsTheSeededReminders() throws Exception {
        mvc.perform(get("/api/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").isNotEmpty());
    }

    @Test
    void createsThenFetchesThenDeletesAReminder() throws Exception {
        String body = """
                {"title":"Pay the electricity bill","notes":"Account 4471","dueDate":"2026-10-02","dueTime":"08:15"}
                """;

        MvcResult created = mvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Pay the electricity bill"))
                .andExpect(jsonPath("$.done").value(false))
                .andReturn();

        JsonNode node = json.readTree(created.getResponse().getContentAsString());
        String id = node.get("id").asText();

        mvc.perform(get("/api/reminders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Account 4471"));

        mvc.perform(delete("/api/reminders/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/reminders/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsAReminderWithNoTitle() throws Exception {
        String body = """
                {"title":"  ","dueDate":"2026-10-02"}
                """;

        mvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.problems[0]").value("title: title is required"));
    }

    @Test
    void rejectsAReminderWithNoDueDate() throws Exception {
        String body = """
                {"title":"Something"}
                """;

        mvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownIdReturnsNotFound() throws Exception {
        mvc.perform(get("/api/reminders/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void calendarGroupsRemindersByDay() throws Exception {
        LocalDate soon = LocalDate.now().plusMonths(6).withDayOfMonth(14);
        String body = """
                {"title":"Dentist","dueDate":"%s"}
                """.formatted(soon);

        mvc.perform(post("/api/reminders")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/calendar/" + soon.getYear() + "/" + soon.getMonthValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(soon.getYear()))
                .andExpect(jsonPath("$.month").value(soon.getMonthValue()))
                .andExpect(jsonPath("$.days[?(@.date == '" + soon + "')].count").value(1));
    }
}
