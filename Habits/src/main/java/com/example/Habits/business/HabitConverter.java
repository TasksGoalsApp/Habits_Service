package com.example.Habits.business;

import com.example.Habits.domain.Habit;
import com.example.Habits.domain.TimeOfDay;
import com.example.Habits.domain.Weekdays;
import com.example.Habits.repository.HabitEntity;
import org.springframework.stereotype.Component;


public final class HabitConverter {
    private HabitConverter(){}

    public static Habit toDomain(HabitEntity entity){
        return Habit.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .habitCategory(entity.getHabitCategory())
                .habitFrequency(entity.getHabitFrequency())
                .description(entity.getDescription())
                .weekdays(Weekdays.normalize(entity.getWeekdays()))
                .timeOfDay(entity.getTimeOfDay() == null ? TimeOfDay.ANYTIME : entity.getTimeOfDay())
                .target(entity.getTarget())
                .currentStreak(entity.getCurrentStreak())
                .bestStreak(entity.getBestStreak())
                .createdAt(entity.getCreatedAt())
                .active(entity.isActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void applyToEntity(Habit habit, HabitEntity entity) {
        entity.setName(habit.getName());
        entity.setHabitFrequency(habit.getHabitFrequency());
        entity.setHabitCategory(habit.getHabitCategory());
        entity.setDescription(habit.getDescription());
        entity.setWeekdays(Weekdays.normalize(habit.getWeekdays()));
        entity.setTimeOfDay(habit.getTimeOfDay() == null ? TimeOfDay.ANYTIME : habit.getTimeOfDay());
        entity.setTarget(habit.getTarget());
        entity.setCurrentStreak(habit.getCurrentStreak());
        entity.setBestStreak(habit.getBestStreak());
        entity.setActive(habit.isActive());
    }


}
