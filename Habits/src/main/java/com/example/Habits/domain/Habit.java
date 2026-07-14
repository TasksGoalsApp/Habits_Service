package com.example.Habits.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class Habit {

    private Long id;
    private Long userId;
    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;
    @Builder.Default
    private int currentStreak=0;
    @Builder.Default
    private int bestStreak=0;
    @Builder.Default
    private boolean active= true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
