package com.example.Habits.domain.Response.HabitCompletion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class HabitCompletionResponse {
    private Long id;
    private LocalDate completionDate;
}
