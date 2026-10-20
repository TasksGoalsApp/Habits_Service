package com.example.Habits.domain.Response;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.TimeOfDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class HabitResponse {
    private Long id;
    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;
    private String description;
    private Set<DayOfWeek> weekdays;
    private TimeOfDay timeOfDay;
    private String target;
    private LocalDateTime createdAt;

    private int currentStreak;
    private int bestStreak;

    private boolean active;

    private boolean completedForCurrentPeriod;
}
