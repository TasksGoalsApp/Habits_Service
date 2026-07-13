package com.example.Habits.business.Impl;

import com.example.Habits.business.IDeactivateHabit;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeactivateHabitImpl implements IDeactivateHabit {

    private final HabitsRepository habitsRepository;


    @Override
    @Transactional
    public void deactivateHabit(Long habitId, Long userId) {
        HabitEntity entity = habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(()-> new HabitNotFoundException(habitId));

        entity.setActive(false);

    }
}
