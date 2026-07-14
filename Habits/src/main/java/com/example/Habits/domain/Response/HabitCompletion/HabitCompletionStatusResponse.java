package com.example.Habits.domain.Response.HabitCompletion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter
public class HabitCompletionStatusResponse {
    private Long habitId;
    private boolean completed;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private LocalDate completionDate;
}
