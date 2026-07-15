package com.example.Habits.business.Impl;

import com.example.Habits.business.HabitConverter;
import com.example.Habits.business.IActivateHabit;
import com.example.Habits.domain.Habit;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ActivateHabitImpl implements IActivateHabit {
    private final HabitsRepository habitsRepository;
    private final HabitConverter habitConverter;

    @Transactional
    @Override
    public void activateHabit(Long habitId, Long userId) {
        HabitEntity entity = habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(()-> new HabitNotFoundException(habitId));

        Habit habit = HabitConverter.toDomain(entity);
        habit.activate();
        habitConverter.applyToEntity(habit, entity);
    }
}
