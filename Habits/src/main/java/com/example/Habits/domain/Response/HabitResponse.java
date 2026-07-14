package com.example.Habits.domain.Response;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;

public class HabitResponse {
    private Long id;
    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;

    private int currentStreak;
    private int bestStreak;

    private boolean active;

    private boolean completedForCurrentPeriod;
}
