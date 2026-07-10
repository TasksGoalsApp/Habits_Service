package com.example.Habits.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class HabitCompletion {
    private Long id;
    private Long habitId;
    private LocalDate completionDate;
    private LocalDateTime createdAt;

}
