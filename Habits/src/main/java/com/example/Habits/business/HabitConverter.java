package com.example.Habits.business;

import com.example.Habits.domain.Habit;
import com.example.Habits.repository.HabitEntity;
import org.springframework.stereotype.Component;

@Component
public class HabitConverter {
    private HabitConverter(){}

    public static Habit toDomain(HabitEntity entity){
        return Habit.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .habitCategory(entity.getHabitCategory())
                .habitFrequency(entity.getHabitFrequency())
                .currentStreak(entity.getCurrentStreak())
                .bestStreak(entity.getBestStreak())
                .createdAt(entity.getCreatedAt())
                .active(entity.isActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void applyToEntity(Habit habit, HabitEntity entity) {
        entity.setName(habit.getName());
        entity.setHabitFrequency(habit.getHabitFrequency());
        entity.setHabitCategory(habit.getHabitCategory());
        entity.setCurrentStreak(habit.getCurrentStreak());
        entity.setBestStreak(habit.getBestStreak());
        entity.setActive(habit.isActive());
    }


}
