package com.example.Habits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HabitScheduleApiTests {
    @Autowired MockMvc mvc;

    private static final String BASE = "{\"name\":\"Meditate\",\"habitFrequency\":\"DAILY\",\"habitCategory\":\"MINDFULNESS\"%s}";

    private String auth(long userId) throws Exception {
        return "Bearer " + TestTokens.token(userId, "CUSTOMER");
    }

    private long create(long userId, String extra) throws Exception {
        MvcResult result = mvc.perform(post("/habits").header("Authorization", auth(userId))
                        .contentType(MediaType.APPLICATION_JSON).content(BASE.formatted(extra)))
                .andExpect(status().isCreated()).andReturn();
        String body = result.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test void createWithoutNewFieldsUsesDefaults() throws Exception {
        long id = create(9001, "");
        mvc.perform(get("/habits").header("Authorization", auth(9001)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habits[0].id").value(id))
                .andExpect(jsonPath("$.habits[0].timeOfDay").value("ANYTIME"))
                .andExpect(jsonPath("$.habits[0].weekdays", hasSize(7)))
                .andExpect(jsonPath("$.habits[0].weekdays[0]").value("MONDAY"))
                .andExpect(jsonPath("$.habits[0].description").value(nullValue()))
                .andExpect(jsonPath("$.habits[0].target").value(nullValue()));
    }

    @Test void createAndUpdateRoundTripNewFields() throws Exception {
        long id = create(9002, ",\"description\":\"Quiet\",\"weekdays\":[\"FRIDAY\",\"MONDAY\"],\"timeOfDay\":\"MORNING\",\"target\":\"10 min\"");
        mvc.perform(get("/habits").header("Authorization", auth(9002)))
                .andExpect(jsonPath("$.habits[0].description").value("Quiet"))
                .andExpect(jsonPath("$.habits[0].weekdays", contains("MONDAY", "FRIDAY")))
                .andExpect(jsonPath("$.habits[0].timeOfDay").value("MORNING"))
                .andExpect(jsonPath("$.habits[0].target").value("10 min"))
                .andExpect(jsonPath("$.habits[0].createdAt").exists());

        mvc.perform(put("/habits/" + id).header("Authorization", auth(9002)).contentType(MediaType.APPLICATION_JSON)
                        .content(BASE.formatted(",\"weekdays\":[\"SUNDAY\"],\"timeOfDay\":\"EVENING\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekdays", contains("SUNDAY")))
                .andExpect(jsonPath("$.timeOfDay").value("EVENING"))
                .andExpect(jsonPath("$.description").value(nullValue()));
    }

    @Test void badNewFieldValuesAreRejectedWithBadRequest() throws Exception {
        String[] bad = {
                ",\"description\":\"" + "x".repeat(281) + "\"",
                ",\"target\":\"" + "x".repeat(41) + "\"",
                ",\"timeOfDay\":\"NOON\"",
                ",\"weekdays\":[\"FUNDAY\"]",
                ",\"weekdays\":[null]"};
        for (String extra : bad) {
            mvc.perform(post("/habits").header("Authorization", auth(9003)).contentType(MediaType.APPLICATION_JSON)
                            .content(BASE.formatted(extra)))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/habits/1").header("Authorization", auth(9003)).contentType(MediaType.APPLICATION_JSON)
                            .content(BASE.formatted(extra)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test void backfillRulesAndDeleteByDate() throws Exception {
        long id = create(9004, "");
        LocalDate today = LocalDate.now();

        // future and before-creation dates are 400
        mvc.perform(post("/habits/" + id + "/completions").param("date", today.plusDays(1).toString())
                        .header("Authorization", auth(9004)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/habits/" + id + "/completions").param("date", today.minusDays(1).toString())
                        .header("Authorization", auth(9004)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/habits/" + id + "/completions").param("date", "not-a-date")
                        .header("Authorization", auth(9004)))
                .andExpect(status().isBadRequest());

        // another user cannot touch it
        mvc.perform(post("/habits/" + id + "/completions").header("Authorization", auth(9005)))
                .andExpect(status().isNotFound());

        // explicit date == today, same as no date
        mvc.perform(post("/habits/" + id + "/completions").param("date", today.toString())
                        .header("Authorization", auth(9004)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completionDate").value(today.toString()))
                .andExpect(jsonPath("$.currentStreak").value(1));
        mvc.perform(post("/habits/" + id + "/completions").header("Authorization", auth(9004)))
                .andExpect(status().isConflict());

        mvc.perform(get("/habits/" + id + "/completions").header("Authorization", auth(9004)))
                .andExpect(jsonPath("$.completions[0].createdAt").exists());

        // delete by date
        mvc.perform(delete("/habits/" + id + "/completions/" + today).header("Authorization", auth(9004)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/habits/" + id + "/completions/" + today).header("Authorization", auth(9004)))
                .andExpect(status().isConflict());
        mvc.perform(delete("/habits/" + id + "/completions/nope").header("Authorization", auth(9004)))
                .andExpect(status().isBadRequest());

        // current still works
        mvc.perform(post("/habits/" + id + "/completions").header("Authorization", auth(9004)))
                .andExpect(status().isCreated());
        mvc.perform(delete("/habits/" + id + "/completions/current").header("Authorization", auth(9004)))
                .andExpect(status().isNoContent());

        // paused habits cannot be completed
        mvc.perform(patch("/habits/" + id + "/deactivate").header("Authorization", auth(9004)))
                .andExpect(status().isNoContent());
        mvc.perform(post("/habits/" + id + "/completions").param("date", today.toString())
                        .header("Authorization", auth(9004)))
                .andExpect(status().isConflict());
    }
}
