package com.example.Habits.domain.Response.HabitCompletion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class HabitCompletionResponse {
    private Long id;
    private LocalDate completionDate;
    private LocalDateTime createdAt;
}
