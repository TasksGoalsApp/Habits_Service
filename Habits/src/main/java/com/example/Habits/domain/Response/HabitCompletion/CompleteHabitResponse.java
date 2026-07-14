package com.example.Habits.domain.Response.HabitCompletion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
@Getter
@Builder
@AllArgsConstructor
public class CompleteHabitResponse {
    private Long completionId;
    private Long habitId;
    private LocalDate completionDate;
    private int currentStreak;
    private int bestStreak;
}
