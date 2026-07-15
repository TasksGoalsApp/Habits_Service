package com.example.Habits.business.Impl;

import com.example.Habits.business.HabitConverter;
import com.example.Habits.business.IGetAllHabitsByUser;
import com.example.Habits.domain.Habit;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.GetAllHabitsByUserResponse;
import com.example.Habits.domain.Response.HabitResponse;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@AllArgsConstructor
public class GetAllHabitsByUserImpl implements IGetAllHabitsByUser {
    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository completionRepository;


    @Override
    public GetAllHabitsByUserResponse getAllHabits(Long userId) {
        LocalDate today = LocalDate.now();

        List<HabitResponse> habits = habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(habit -> HabitResponse.builder()
                        .id(habit.getId())
                        .name(habit.getName())
                        .habitFrequency(habit.getHabitFrequency())
                        .habitCategory(habit.getHabitCategory())
                        .currentStreak(habit.getCurrentStreak())
                        .bestStreak(habit.getBestStreak())
                        .active(habit.isActive())
                        .completedForCurrentPeriod(isCompletedForCurrentPeriod(habit, today))
                        .build()
                )
                .toList();

        return GetAllHabitsByUserResponse.builder()
                .habits(habits)
                .build();
    }


    private boolean isCompletedForCurrentPeriod(HabitEntity habit, LocalDate today) {
        if (habit.getHabitFrequency() == HabitFrequency.DAILY) {
            return completionRepository.existsByHabitIdAndCompletionDate(habit.getId(), today);
        }

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        return completionRepository.existsByHabitIdAndCompletionDateBetween(habit.getId(), weekStart, weekEnd);
    }
}
