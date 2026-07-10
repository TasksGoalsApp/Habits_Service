package com.example.Habits.business;

import com.example.Habits.domain.Habit;
import com.example.Habits.repository.HabitEntity;

public class HabitConvertor {
    private HabitConvertor(){}

    private static Habit convert(HabitEntity entity){
        return Habit.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .habitCategory(entity.getHabitCategory())
                .habitFrequency(entity.getHabitFrequency())
                .currentStreak(entity.getCurrentStreak())
                .bestStreak(entity.getBestStreak())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
